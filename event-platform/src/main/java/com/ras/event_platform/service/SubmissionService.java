package com.ras.event_platform.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ras.event_platform.dto.SubmissionRequest;
import com.ras.event_platform.dto.TeamStatusResponse;
import com.ras.event_platform.model.Registration;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.repo.RegistrationRepository;
import com.ras.event_platform.repo.SubmissionRepository;

@Service
public class SubmissionService {

  @Autowired
  private SubmissionRepository submissionRepository;
  @Autowired
  private RegistrationRepository registrationRepository;

  public Boolean hasPendingSubmission(UUID teamId, Integer roundNumber, String taskId) {
    return submissionRepository.hasPendingSubmission(teamId, "PENDING", roundNumber, taskId);
  }

  public Submission create(UUID teamId, SubmissionRequest request) {

    Registration registration = registrationRepository.findById(teamId)
        .orElseThrow(() -> new RuntimeException("Team Not Found"));

    // creates a new object for each submission
    Submission submission = new Submission();
    submission.setRegistrationId(registration.getId());
    submission.setRoundNumber(request.getRoundNumber());
    submission.setTaskId(request.getTaskId());
    submission.setPayload(convertToJson(request));
    submission.setStatus("PENDING");

    return submissionRepository.save(submission);
  }

  public List<Submission> getTeamSubmissions(UUID teamId) {
    return submissionRepository.findByRegistrationId(teamId);
  }

  public TeamStatusResponse getTeamStatus(UUID teamId) {
    List<Submission> submissions = submissionRepository.findByRegistrationId(teamId);
    
    int highestState = 0;
    boolean isPending = false;
    
    for (Submission sub : submissions) {
      if ("PENDING".equals(sub.getStatus())) {
        isPending = true;
      }
      if ("APPROVED".equals(sub.getStatus())) {
        if (sub.getTaskId() != null && sub.getTaskId().startsWith("FEATURE-")) {
          try {
            int num = Integer.parseInt(sub.getTaskId().replace("FEATURE-", ""));
            if (num > highestState) highestState = num;
          } catch(Exception e) {}
        } else if ("ROUND-2".equals(sub.getTaskId())) {
          if (highestState < 6) highestState = 6;
        } else if ("ROUND-3".equals(sub.getTaskId())) {
          if (highestState < 7) highestState = 7;
        }
      }
    }
    
    int nextState = highestState + 1;
    
    String allowedTaskId;
    int allowedRound;
    
    if (nextState <= 5) {
      allowedTaskId = "FEATURE-" + nextState;
      allowedRound = 1;
    } else if (nextState == 6) {
      allowedTaskId = "ROUND-2";
      allowedRound = 2;
    } else if (nextState == 7) {
      allowedTaskId = "ROUND-3";
      allowedRound = 3;
    } else {
      allowedTaskId = "COMPLETED";
      allowedRound = 3;
    }
    
    return new TeamStatusResponse(allowedTaskId, allowedRound, isPending);
  }

  private String convertToJson(SubmissionRequest request) {
    // Use Jackson ObjectMapper to convert DTO to JSON string
    return String.format("{\"githubUrl\": \"%s\", \"description\": \"%s\"}",
        request.getGithubUrl(), request.getDescription());
  }
}
