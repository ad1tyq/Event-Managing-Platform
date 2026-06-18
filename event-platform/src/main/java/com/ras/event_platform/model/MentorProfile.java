package com.ras.event_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mentor_profiles")
public class MentorProfile {
    @Id
    @Column(name = "user_id")
    private Long userId; // Matches users table ID

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "current_status", nullable = false)
    private String currentStatus = "AVAILABLE"; // AVAILABLE, BUSY

    // Transient fields for frontend convenience
    @Transient
    private String username;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
