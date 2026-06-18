package com.ras.event_platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ras.event_platform.model.Evaluation;
import com.ras.event_platform.model.Event;
import com.ras.event_platform.model.Registration;
import com.ras.event_platform.model.Submission;
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

      // Update the running total score
      double currentTotal = registration.getTotalScore() != null ? registration.getTotalScore() : 0.0;
      registration.setTotalScore(currentTotal + averageScore);
      registrationRepository.save(registration);
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

    return submissionRepository.save(submission);
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
}
