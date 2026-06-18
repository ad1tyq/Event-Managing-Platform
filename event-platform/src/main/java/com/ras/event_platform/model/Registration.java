package com.ras.event_platform.model;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Map;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "registrations")
public class Registration {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "event_id", nullable = false)
  private int eventId;

  @Column(name = "unstop_team_id", nullable = false)
  private String unstopTeamId;

  @Column(name = "team_name", nullable = false)
  private String teamName;

  @Column(name = "team_passcode", nullable = false)
  private String teamPasscode;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "progress_state", columnDefinition = "jsonb")
  private Map<String, Object> progressState;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "member_details", columnDefinition = "jsonb")
  private String memberDetails;

  @Column(name = "total_score")
  private Double totalScore = 0.0;

  @Column(name = "registered_at", updatable = false)
  private LocalDateTime registeredAt;

  public Registration() {
    // Default values for new objects
    this.progressState = new java.util.HashMap<>();
    this.registeredAt = LocalDateTime.now();
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public int getEventId() {
    return this.eventId;
  }

  public UUID getId() {
    return this.id;
  }

  public void setUnstopTeamId(String unstopTeamId) {
    this.unstopTeamId = unstopTeamId;
  }

  public String getUnstopTeamId() {
    return this.unstopTeamId;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }

  public String getTeamName() {
    return this.teamName;
  }

  public void setTeamPasscode(String teamPasscode) {
    this.teamPasscode = teamPasscode;
  }

  public String getTeamPasscode() {
    return this.teamPasscode;
  }

  public void setMemberDetails(String memberDetails) {
    this.memberDetails = memberDetails;
  }

  public String getMemberDetails() {
    return this.memberDetails;
  }

  public java.util.Map<String, Object> getProgressState() {
    return this.progressState;
  }

  public void setProgressState(java.util.Map<String, Object> progressState) {
    this.progressState = progressState;
  }

  public LocalDateTime getRegisteredAt() {
    return this.registeredAt;
  }

  public void setRegisteredAt(LocalDateTime registeredAt) {
    this.registeredAt = registeredAt;
  }

  public Double getTotalScore() {
    return this.totalScore;
  }

  public void setTotalScore(Double totalScore) {
    this.totalScore = totalScore;
  }
}
