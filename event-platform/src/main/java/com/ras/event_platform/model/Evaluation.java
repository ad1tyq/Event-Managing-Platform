package com.ras.event_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "submission_id", "judge_id" })
})
public class Evaluation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "submission_id", nullable = false)
  private Integer submissionId;

  @Column(name = "judge_id", nullable = false)
  private Integer judgeId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "score_breakdown", columnDefinition = "jsonb")
  private String scoreBreakdown; // e.g., {"ui": 8, "logic": 9}

  @Column(name = "total_score", nullable = false)
  private Integer totalScore;

  @Column(columnDefinition = "TEXT")
  private String feedback;

  @Column(name = "graded_at", updatable = false)
  private LocalDateTime gradedAt = LocalDateTime.now();

  public Evaluation() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Integer submissionId) {
    this.submissionId = submissionId;
  }

  public Integer getJudgeId() {
    return judgeId;
  }

  public void setJudgeId(Integer judgeId) {
    this.judgeId = judgeId;
  }

  public String getScoreBreakdown() {
    return scoreBreakdown;
  }

  public void setScoreBreakdown(String scoreBreakdown) {
    this.scoreBreakdown = scoreBreakdown;
  }

  public Integer getTotalScore() {
    return totalScore;
  }

  public void setTotalScore(Integer totalScore) {
    this.totalScore = totalScore;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public LocalDateTime getGradedAt() {
    return gradedAt;
  }

  public void setGradedAt(LocalDateTime gradedAt) {
    this.gradedAt = gradedAt;
  }
}
