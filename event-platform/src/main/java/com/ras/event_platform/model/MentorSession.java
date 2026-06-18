package com.ras.event_platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mentor_sessions")
public class MentorSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "registration_id", nullable = false)
    private UUID registrationId;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Column(name = "issue_description", columnDefinition = "TEXT", nullable = false)
    private String issueDescription;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(nullable = false)
    private String status = "REQUESTED"; // REQUESTED, ACTIVE, RESOLVED, CANCELLED

    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Transient
    private String teamName;
    
    @Transient
    private String mentorName;

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

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }
}
