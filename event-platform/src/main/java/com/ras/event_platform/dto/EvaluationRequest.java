package com.ras.event_platform.dto;

import java.util.Map;

public class EvaluationRequest {
  private Integer submissionId;
  private Map<String, Integer> scoreBreakdown; // {"functionality": 10, "ui": 8}
  private String feedback;

  public Integer getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Integer submissionId) {
    this.submissionId = submissionId;
  }

  public Map<String, Integer> getScoreBreakdown() {
    return scoreBreakdown;
  }

  public void setScoreBreakdown(Map<String, Integer> scoreBreakdown) {
    this.scoreBreakdown = scoreBreakdown;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }
}
