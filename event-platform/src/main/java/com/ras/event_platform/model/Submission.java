package com.ras.event_platform.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "submissions")
public class Submission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "registration_id", nullable = false)
  private UUID registrationId;

  @Column(name = "round_number", nullable = false)
  private Integer roundNumber;

  @Column(name = "task_id", nullable = false)
  private String taskId;

  @Column(name = "rejection_reason", columnDefinition = "TEXT")
  private String rejectionReason;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private String payload;

  @Column(nullable = false)
  private String status = "PENDING"; // APPROVED, REJECTED

  @Column(name = "average_score")
  private Double averageScore;

  @Column(name = "submitted_at", updatable = false)
  private LocalDateTime submittedAt = LocalDateTime.now();

  @Column(name = "submission_type")
  private String submissionType = "COMMIT";

  @Transient
  private String teamName;

  public Submission() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public UUID getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(UUID registrationId) {
    this.registrationId = registrationId;
  }

  public Integer getRoundNumber() {
    return roundNumber;
  }

  public void setRoundNumber(Integer roundNumber) {
    this.roundNumber = roundNumber;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  public Double getAverageScore() {
    return averageScore;
  }

  public void setAverageScore(Double averageScore) {
    this.averageScore = averageScore;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }

  public String getSubmissionType() {
    return submissionType;
  }

  public void setSubmissionType(String submissionType) {
    this.submissionType = submissionType;
  }
}
