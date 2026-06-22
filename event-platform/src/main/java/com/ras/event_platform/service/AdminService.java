package com.ras.event_platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ras.event_platform.model.Evaluation;
import com.ras.event_platform.model.Event;
import com.ras.event_platform.model.Registration;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.repo.DemoCallRepository;
import com.ras.event_platform.repo.EvaluationRepository;
import com.ras.event_platform.repo.EventRepository;
import com.ras.event_platform.repo.RegistrationRepository;
import com.ras.event_platform.repo.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

  @Autowired
  private SubmissionRepository submissionRepository;

  @Autowired
  private EvaluationRepository evaluationRepository;

  @Autowired
  private RegistrationRepository registrationRepository;

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private DemoCallRepository demoCallRepository;

  public Submission finalizeSubmission(Integer submissionId) {

    Submission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new RuntimeException("Submission not found"));

    if (!"PENDING".equals(submission.getStatus())) {
      throw new IllegalStateException("This submission has already been finalized as " + submission.getStatus());
    }

    List<Evaluation> evaluations = evaluationRepository.findBySubId(submissionId);

    if (evaluations.isEmpty()) {
      throw new IllegalStateException("Cannot finalize: No judges have evaluated this submission yet.");
    }

    double averageScore = evaluations.stream()
        .mapToInt(Evaluation::getTotalScore)
        .average()
        .orElse(0.0);

    // 1. Traverse relationships to find the Event
    Registration registration = registrationRepository.findById(submission.getRegistrationId())
        .orElseThrow(() -> new RuntimeException("Registration not found for this submission"));

    Event event = eventRepository.findById(registration.getEventId())
        .orElseThrow(() -> new RuntimeException("Event not found"));

    // 2. Dynamically extract passing_threshold from JSONB
    double passingThreshold = 50.0; // The fallback safe value
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode configNode = mapper.readTree(event.getConfig());

      if (configNode.has("passing_threshold")) {
        passingThreshold = configNode.get("passing_threshold").asDouble();
      }
    } catch (Exception e) {
      System.err.println(
          "CRITICAL WARNING: Event " + event.getId() + " has malformed JSON config. Falling back to threshold 50.0");
    }

    // 3. The Dynamic State Evaluation
    submission.setAverageScore(averageScore);

    if (averageScore >= passingThreshold) {
      submission.setStatus("APPROVED");
    } else {
      submission.setStatus("REJECTED");

      String combinedFeedback = evaluations.stream()
          .map(Evaluation::getFeedback)
          .filter(feedback -> feedback != null && !feedback.trim().isEmpty())
          .collect(Collectors.joining(" | "));

      if (combinedFeedback.isEmpty()) {
        combinedFeedback = "Your submission scored " + averageScore
            + " points. This did not meet the required event threshold of " + passingThreshold + " points.";
      }

      submission.setRejectionReason(combinedFeedback);
    }

    submission = submissionRepository.save(submission);
    recalculateTotalScore(registration);
    
    if ("APPROVED".equals(submission.getStatus()) && "ROUND-2".equals(submission.getTaskId())) {
        provisionRound3Demo(registration.getId());
    }
    
    if ("DEMO".equals(submission.getSubmissionType()) || "ROUND-3".equals(submission.getTaskId())) {
        demoCallRepository.findBySubmissionId(submission.getId()).ifPresent(dc -> {
            dc.setStatus("COMPLETED");
            dc.setCompletedAt(java.time.LocalDateTime.now());
            demoCallRepository.save(dc);
        });
    }
    
    return submission;
  }

  public void provisionRound3Demo(java.util.UUID regId) {
      if (!submissionRepository.existsByRegistrationIdAndTaskId(regId, "ROUND-3")) {
          Submission sub = new Submission();
          sub.setRegistrationId(regId);
          sub.setRoundNumber(3);
          sub.setTaskId("ROUND-3");
          sub.setSubmissionType("DEMO");
          sub.setPayload("{\"githubUrl\": \"Live Call\", \"description\": \"Round 3 Live Meeting Evaluation\"}");
          sub.setStatus("PENDING");
          sub = submissionRepository.save(sub);
          
          com.ras.event_platform.model.DemoCall dc = new com.ras.event_platform.model.DemoCall();
          dc.setSubmissionId(sub.getId());
          dc.setStatus("QUEUED");
          demoCallRepository.save(dc);
      }
  }

  private void recalculateTotalScore(Registration registration) {
      List<Submission> allSubs = submissionRepository.findByRegistrationId(registration.getId());
      
      java.util.Map<String, Double> maxScores = new java.util.HashMap<>();
      for (Submission s : allSubs) {
          if ("APPROVED".equals(s.getStatus()) && s.getAverageScore() != null) {
              double currentMax = maxScores.getOrDefault(s.getTaskId(), 0.0);
              if (s.getAverageScore() > currentMax) {
                  maxScores.put(s.getTaskId(), s.getAverageScore());
              }
          }
      }
      
      double total = maxScores.values().stream().mapToDouble(Double::doubleValue).sum();
      registration.setTotalScore(total);
      registrationRepository.save(registration);
  }

  public Event updateGlobalRound(Integer eventId, Integer newRound) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new RuntimeException("Event not found"));

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode configNode = mapper.readTree(event.getConfig());

      if (configNode.has("total_rounds")) {
        int totalRounds = configNode.get("total_rounds").asInt();
        if (newRound > totalRounds) {
          throw new IllegalStateException("Cannot increase global round beyond the total rounds configured (" + totalRounds + ")");
        }
      }
    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      System.err.println("Warning: Could not parse total_rounds from event config.");
    }

    event.setCurrentGlobalRound(newRound);
    return eventRepository.save(event);
  }

  private Event updateEventConfigField(Integer eventId, String fieldName, Object value) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new RuntimeException("Event not found"));
    try {
      ObjectMapper mapper = new ObjectMapper();
      com.fasterxml.jackson.databind.node.ObjectNode configNode = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(event.getConfig());

      if (value instanceof String) {
          configNode.put(fieldName, (String) value);
      } else if (value instanceof Boolean) {
          configNode.put(fieldName, (Boolean) value);
      } else if (value instanceof Integer) {
          configNode.put(fieldName, (Integer) value);
      }

      event.setConfig(mapper.writeValueAsString(configNode));
      return eventRepository.save(event);
    } catch (Exception e) {
      throw new RuntimeException("Failed to update event config", e);
    }
  }

  public Event updateMeetingLink(Integer eventId, String meetingLink) {
      return updateEventConfigField(eventId, "meeting_link", meetingLink);
  }

  public Event setActiveMeetingTeam(Integer eventId, String teamId) {
      Event updatedEvent = updateEventConfigField(eventId, "active_meeting_team_id", teamId);
      return updatedEvent;
  }

  public Event toggleLeaderboard(Integer eventId, boolean isPublished) {
      return updateEventConfigField(eventId, "is_leaderboard_published", isPublished);
  }
}
