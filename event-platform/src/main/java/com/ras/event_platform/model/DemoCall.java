package com.ras.event_platform.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "demo_calls")
public class DemoCall {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "submission_id", nullable = false, unique = true)
  private Integer submissionId;

  @Column(name = "judge_id")
  private Long judgeId;

  @Column(name = "meeting_link")
  private String meetingLink;

  @Column(nullable = false)
  private String status = "QUEUED"; // QUEUED, CALLED, COMPLETED

  @Column(name = "called_at")
  private LocalDateTime calledAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submission_id", insertable = false, updatable = false)
  private Submission submission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "judge_id", insertable = false, updatable = false)
  private User judge;

  @Transient
  private String teamName;

  @Transient
  private LocalDateTime queueEnteredAt;

  @Transient
  private String registrationId;

  public DemoCall() {}

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

  public Long getJudgeId() {
    return judgeId;
  }

  public void setJudgeId(Long judgeId) {
    this.judgeId = judgeId;
  }

  public String getMeetingLink() {
    return meetingLink;
  }

  public void setMeetingLink(String meetingLink) {
    this.meetingLink = meetingLink;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getCalledAt() {
    return calledAt;
  }

  public void setCalledAt(LocalDateTime calledAt) {
    this.calledAt = calledAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime completedAt) {
    this.completedAt = completedAt;
  }

  public Submission getSubmission() {
    return submission;
  }

  public User getJudge() {
    return judge;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }

  public LocalDateTime getQueueEnteredAt() {
    return queueEnteredAt;
  }

  public void setQueueEnteredAt(LocalDateTime queueEnteredAt) {
    this.queueEnteredAt = queueEnteredAt;
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }
}
