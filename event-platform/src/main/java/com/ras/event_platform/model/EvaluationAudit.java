package com.ras.event_platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_audits")
public class EvaluationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluation_id", nullable = false)
    private Long evaluationId;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "old_score_breakdown", columnDefinition = "jsonb")
    private String oldScoreBreakdown;

    @Column(name = "old_total_score", nullable = false)
    private Integer oldTotalScore;

    @Column(name = "old_feedback", columnDefinition = "TEXT")
    private String oldFeedback;

    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getOldScoreBreakdown() {
        return oldScoreBreakdown;
    }

    public void setOldScoreBreakdown(String oldScoreBreakdown) {
        this.oldScoreBreakdown = oldScoreBreakdown;
    }

    public Integer getOldTotalScore() {
        return oldTotalScore;
    }

    public void setOldTotalScore(Integer oldTotalScore) {
        this.oldTotalScore = oldTotalScore;
    }

    public String getOldFeedback() {
        return oldFeedback;
    }

    public void setOldFeedback(String oldFeedback) {
        this.oldFeedback = oldFeedback;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
