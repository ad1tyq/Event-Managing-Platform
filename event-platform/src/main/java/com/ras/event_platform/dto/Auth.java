package com.ras.event_platform.dto;

import lombok.Data;

@Data
public class Auth {
  private String teamName;
  private String teamPasscode;

  public String getTeamName() {
    return this.teamName;
  }

  public String getTeamPasscode() {
    return this.teamPasscode;
  }
}
