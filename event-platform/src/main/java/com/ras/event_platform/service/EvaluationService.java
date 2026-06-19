package com.ras.event_platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ras.event_platform.dto.EvaluationRequest;
import com.ras.event_platform.model.Evaluation;
import com.ras.event_platform.model.EvaluationAudit;
import com.ras.event_platform.model.Registration;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.repo.EvaluationAuditRepository;
import com.ras.event_platform.repo.EvaluationRepository;
import com.ras.event_platform.repo.RegistrationRepository;
import com.ras.event_platform.repo.SubmissionRepository;
import com.ras.event_platform.repo.EventRepository;
import com.ras.event_platform.model.Event;
import java.util.List;
import java.util.Optional;

@Service
@Component
public class EvaluationService {

  @Autowired
  private EvaluationRepository evaluationRepository;

  @Autowired
  private SubmissionRepository submissionRepository;

  @Autowired
  private EvaluationAuditRepository evaluationAuditRepository;

  @Autowired
  private RegistrationRepository registrationRepository;

  @Autowired
  private EventRepository eventRepository;

  public Evaluation gradeSubmission(Long judgeId, EvaluationRequest request) {
    // 1. Verify submission exists
    Submission submission = submissionRepository.findById(request.getSubmissionId())
        .orElseThrow(() -> new RuntimeException("Submission not found"));

    // 2. Validate Game State (Allow PENDING, GRADED, APPROVED, REJECTED if upserting)
    // Only block if we're trying to evaluate something completely invalid.
    // Actually, we shouldn't block upserts based on PENDING anymore since they can edit APPROVED submissions.

    // 3. Calculate Total Score mathematically on the backend (Average of categories)
    double categoryAverage = request.getScoreBreakdown().values().stream()
        .mapToInt(Integer::intValue)
        .average()
        .orElse(0.0);
    int calculatedTotal = (int) Math.round(categoryAverage);

    String newScoreBreakdownJson;
    try {
      ObjectMapper mapper = new ObjectMapper();
      newScoreBreakdownJson = mapper.writeValueAsString(request.getScoreBreakdown());
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to process score breakdown JSON.");
    }

    Evaluation evaluation;
    Optional<Evaluation> existingOpt = evaluationRepository.findBySubmissionIdAndJudgeId(submission.getId(), judgeId);
    
    if (existingOpt.isPresent()) {
        evaluation = existingOpt.get();
        // Create Audit Trail
        EvaluationAudit audit = new EvaluationAudit();
        audit.setEvaluationId(Long.valueOf(evaluation.getId()));
        audit.setOldScoreBreakdown(evaluation.getScoreBreakdown());
        audit.setOldTotalScore(evaluation.getTotalScore());
        audit.setOldFeedback(evaluation.getFeedback());
        evaluationAuditRepository.save(audit);

        // Update existing
        evaluation.setTotalScore(calculatedTotal);
        evaluation.setFeedback(request.getFeedback());
        evaluation.setScoreBreakdown(newScoreBreakdownJson);
    } else {
        if (!"PENDING".equals(submission.getStatus())) {
            throw new IllegalStateException("Cannot grade this submission. It is already marked as " + submission.getStatus());
        }
        // Create new
        evaluation = new Evaluation();
        evaluation.setSubmissionId(submission.getId());
        evaluation.setJudgeId(judgeId);
        evaluation.setTotalScore(calculatedTotal);
        evaluation.setFeedback(request.getFeedback());
        evaluation.setScoreBreakdown(newScoreBreakdownJson);
    }

    evaluation = evaluationRepository.save(evaluation);

    // Math Brain Live Recalculation if already APPROVED or REJECTED
    if ("APPROVED".equals(submission.getStatus()) || "REJECTED".equals(submission.getStatus())) {
        List<Evaluation> allEvaluations = evaluationRepository.findBySubId(submission.getId());
        double newAverage = allEvaluations.stream()
            .mapToInt(Evaluation::getTotalScore)
            .average()
            .orElse(0.0);
            
        double oldAverage = submission.getAverageScore() != null ? submission.getAverageScore() : 0.0;
        String oldStatus = submission.getStatus();

        Registration registration = registrationRepository.findById(submission.getRegistrationId())
            .orElseThrow(() -> new RuntimeException("Registration not found"));

        Event event = eventRepository.findById(registration.getEventId())
            .orElseThrow(() -> new RuntimeException("Event not found"));

        double passingThreshold = 50.0;
        try {
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode configNode = mapper.readTree(event.getConfig());
            if (configNode.has("passing_threshold")) {
                passingThreshold = configNode.get("passing_threshold").asDouble();
            }
        } catch (Exception e) { }

        if (newAverage >= passingThreshold) {
            submission.setStatus("APPROVED");
            submission.setRejectionReason(null);
        } else {
            submission.setStatus("REJECTED");
            String combinedFeedback = allEvaluations.stream()
                .map(Evaluation::getFeedback)
                .filter(feedback -> feedback != null && !feedback.trim().isEmpty())
                .collect(java.util.stream.Collectors.joining(" | "));
            if (combinedFeedback.isEmpty()) {
                combinedFeedback = "Your submission scored " + newAverage
                    + " points. This did not meet the required event threshold of " + passingThreshold + " points.";
            }
            submission.setRejectionReason(combinedFeedback);
        }

        submissionRepository.save(submission);
        recalculateTotalScore(registration);
    }

    return evaluation;
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
}
