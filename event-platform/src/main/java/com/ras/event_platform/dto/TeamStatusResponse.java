package com.ras.event_platform.dto;

public class TeamStatusResponse {
    private String allowedTaskId;
    private int allowedRound;
    private boolean isPending;

    public TeamStatusResponse() {
    }

    public TeamStatusResponse(String allowedTaskId, int allowedRound, boolean isPending) {
        this.allowedTaskId = allowedTaskId;
        this.allowedRound = allowedRound;
        this.isPending = isPending;
    }

    public String getAllowedTaskId() {
        return allowedTaskId;
    }

    public void setAllowedTaskId(String allowedTaskId) {
        this.allowedTaskId = allowedTaskId;
    }

    public int getAllowedRound() {
        return allowedRound;
    }

    public void setAllowedRound(int allowedRound) {
        this.allowedRound = allowedRound;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean isPending) {
        this.isPending = isPending;
    }
}
