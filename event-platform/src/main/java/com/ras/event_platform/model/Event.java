package com.ras.event_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "events")
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String name;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private String config;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "current_global_round", nullable = false)
  private Integer currentGlobalRound = 1; // Defaults to Round 1 when the event is created

  // Generate Getters and Setters!
  public Integer getCurrentGlobalRound() { return currentGlobalRound; }
  public void setCurrentGlobalRound(Integer currentGlobalRound) { this.currentGlobalRound = currentGlobalRound; }
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
  }
}
