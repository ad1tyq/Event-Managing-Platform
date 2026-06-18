package com.ras.event_platform.dto;

public class TeamStatusResponse {
    private String allowedTaskId;
    private int allowedRound;
    private boolean isPending;
    private String gmeetLink;
    private int queuePosition;
    private boolean isLeaderboardPublished;

    public TeamStatusResponse() {
    }

    public TeamStatusResponse(String allowedTaskId, int allowedRound, boolean isPending, String gmeetLink,
            int queuePosition, boolean isLeaderboardPublished) {
        this.allowedTaskId = allowedTaskId;
        this.allowedRound = allowedRound;
        this.isPending = isPending;
        this.gmeetLink = gmeetLink;
        this.queuePosition = queuePosition;
        this.isLeaderboardPublished = isLeaderboardPublished;
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

    public String getGmeetLink() {
        return gmeetLink;
    }

    public void setGmeetLink(String gmeetLink) {
        this.gmeetLink = gmeetLink;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public boolean isLeaderboardPublished() {
        return isLeaderboardPublished;
    }

    public void setLeaderboardPublished(boolean isLeaderboardPublished) {
        this.isLeaderboardPublished = isLeaderboardPublished;
    }
}
