package com.ras.event_platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ras.event_platform.dto.EvaluationRequest;
import com.ras.event_platform.model.Evaluation;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.repo.EvaluationRepository;
import com.ras.event_platform.repo.SubmissionRepository;

@Service
@Component
public class EvaluationService {

  @Autowired
  private EvaluationRepository evaluationRepository;

  @Autowired
  private SubmissionRepository submissionRepository;

  public Evaluation gradeSubmission(Long judgeId, EvaluationRequest request) {
    // 1. Verify submission exists
    Submission submission = submissionRepository.findById(request.getSubmissionId())
        .orElseThrow(() -> new RuntimeException("Submission not found"));

    // 2. Validate Game State
    if (!"PENDING".equals(submission.getStatus())) {
      throw new IllegalStateException(
          "Cannot grade this submission. It is already marked as " + submission.getStatus());
    }

    // 3. Calculate Total Score mathematically on the backend
    int calculatedTotal = request.getScoreBreakdown().values().stream()
        .mapToInt(Integer::intValue)
        .sum();

    // 4. Map to Entity
    Evaluation evaluation = new Evaluation();
    evaluation.setSubmissionId(submission.getId());
    evaluation.setJudgeId(judgeId);
    evaluation.setTotalScore(calculatedTotal);
    evaluation.setFeedback(request.getFeedback());

    // 5. Convert Map to JSON String for the database
    try {
      ObjectMapper mapper = new ObjectMapper();
      evaluation.setScoreBreakdown(mapper.writeValueAsString(request.getScoreBreakdown()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to process score breakdown JSON.");
    }

    evaluation = evaluationRepository.save(evaluation);
    return evaluation;
  }
}
