package com.ras.event_platform.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ras.event_platform.dto.SubmissionRequest;
import com.ras.event_platform.dto.TeamStatusResponse;
import com.ras.event_platform.model.Event;
import com.ras.event_platform.model.Registration;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.repo.EventRepository;
import com.ras.event_platform.repo.RegistrationRepository;
import com.ras.event_platform.repo.SubmissionRepository;

@Service
public class SubmissionService {

  @Autowired
  private SubmissionRepository submissionRepository;

  @Autowired
  private RegistrationRepository registrationRepository;

  @Autowired
  private EventRepository eventRepository;

  // Fact: You are still not using this method anywhere in this service. Consider
  // deleting it.
  public Boolean hasPendingSubmission(UUID teamId, Integer roundNumber, String taskId) {
    return submissionRepository.hasPendingSubmission(teamId, "PENDING", roundNumber, taskId);
  }

  public Submission create(UUID teamId, SubmissionRequest request) {
    // 1. Calculate the true state from the database
    TeamStatusResponse status = getTeamStatus(teamId);

    // 2. Enforce the rules at the service layer
    if (status.isPending()) {
      throw new IllegalStateException("You already have a submission pending evaluation.");
    }
    if ("COMPLETED".equals(status.getAllowedTaskId())) {
      throw new IllegalStateException("You have already completed all available tasks for this event.");
    }

    Registration registration = registrationRepository.findById(teamId)
        .orElseThrow(() -> new RuntimeException("Team Not Found"));

    // 3. Create object using Backend-Dictated State
    Submission submission = new Submission();
    submission.setRegistrationId(registration.getId());
    submission.setRoundNumber(status.getAllowedRound()); // SECURITY FIX: Overrides frontend
    submission.setTaskId(status.getAllowedTaskId()); // SECURITY FIX: Overrides frontend
    submission.setPayload(convertToJson(request));
    submission.setStatus("PENDING");

    return submissionRepository.save(submission);
  }

  public List<Submission> getTeamSubmissions(UUID teamId) {
    return submissionRepository.findByRegistrationId(teamId);
  }

  public TeamStatusResponse getTeamStatus(UUID teamId) {
    Registration registration = registrationRepository.findById(teamId)
        .orElseThrow(() -> new RuntimeException("Team not found"));

    Event event = eventRepository.findById(registration.getEventId())
        .orElseThrow(() -> new RuntimeException("Event not found"));

    List<Submission> submissions = submissionRepository.findByRegistrationId(teamId);
    boolean isPending = submissions.stream().anyMatch(sub -> "PENDING".equals(sub.getStatus()));

    try {
      // THE FIX: Dynamic JSON Parsing
      ObjectMapper mapper = new ObjectMapper();
      JsonNode config = mapper.readTree(event.getConfig());
      JsonNode roadmap = config.get("roadmap");

      boolean isLeaderboardPublished = config.has("is_leaderboard_published") && config.get("is_leaderboard_published").asBoolean();
      String meetingLink = config.has("meeting_link") ? config.get("meeting_link").asText() : "";
      String activeMeetingTeamId = config.has("active_meeting_team_id") ? config.get("active_meeting_team_id").asText() : "";

      int highestCompletedStep = 0;

      // Find highest completed step mapped from the JSON array
      for (Submission sub : submissions) {
        if ("APPROVED".equals(sub.getStatus())) {
          for (JsonNode node : roadmap) {
            if (node.get("task_id").asText().equals(sub.getTaskId())) {
              int step = node.get("step").asInt();
              if (step > highestCompletedStep) {
                highestCompletedStep = step;
              }
              break;
            }
          }
        }
      }

      int nextStep = highestCompletedStep + 1;
      String allowedTaskId = "COMPLETED";
      int allowedRound = 3; // Defaults to final round if they finish everything

      // Find the specific task and round for the next step
      for (JsonNode node : roadmap) {
        if (node.get("step").asInt() == nextStep) {
          allowedTaskId = node.get("task_id").asText();
          allowedRound = node.get("round").asInt();
          break;
        }
      }

      // THE SYNCHRONIZATION GATE (The Global Ceiling)
      if (allowedRound > event.getCurrentGlobalRound()) {
          allowedTaskId = "WAITING_ROOM";
          allowedRound = event.getCurrentGlobalRound();
      }

      int queuePosition = 0;
      String gmeetLink = null;

      // ROUND 3 LOGIC (If they are currently supposed to be doing ROUND-3, and aren't blocked by the global ceiling)
      if ("ROUND-3".equals(allowedTaskId)) {
          if (teamId.toString().equals(activeMeetingTeamId)) {
              allowedTaskId = "JOIN_MEETING";
              gmeetLink = meetingLink;
          } else {
              allowedTaskId = "MEETING_WAITING_ROOM";
              // Calculate Queue Position based on when they finished ROUND-2
              List<Submission> round2Approvals = submissionRepository.findByTaskIdAndStatusOrderBySubmittedAtAsc("ROUND-2", "APPROVED");
              for (int i = 0; i < round2Approvals.size(); i++) {
                  if (round2Approvals.get(i).getRegistrationId().equals(teamId)) {
                      queuePosition = i + 1;
                      break;
                  }
              }
          }
      }

      return new TeamStatusResponse(allowedTaskId, allowedRound, isPending, gmeetLink, queuePosition, isLeaderboardPublished);

    } catch (Exception e) {
      System.err.println("CRITICAL: Failed to parse event config JSON for Team " + teamId);
      // Fallback to prevent server crash
      return new TeamStatusResponse("ERROR_PARSING_CONFIG", 1, true, null, 0, false);
    }
  }

  private String convertToJson(SubmissionRequest request) {
    // THE BUG FIX: Never use String.format for JSON creation
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, String> payloadMap = new HashMap<>();
      payloadMap.put("githubUrl", request.getGithubUrl());
      payloadMap.put("description", request.getDescription());
      return mapper.writeValueAsString(payloadMap);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize submission payload safely");
    }
  }
}
