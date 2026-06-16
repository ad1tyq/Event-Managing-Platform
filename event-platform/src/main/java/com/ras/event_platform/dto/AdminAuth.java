package com.ras.event_platform.dto;

import lombok.Data;

@Data
public class AdminAuth {
  private String username;
  private String password;

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }
}
