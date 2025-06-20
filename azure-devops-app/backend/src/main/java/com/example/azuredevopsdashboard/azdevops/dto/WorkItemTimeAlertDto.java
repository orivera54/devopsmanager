package com.example.azuredevopsdashboard.azdevops.dto;

import java.time.Instant;

public class WorkItemTimeAlertDto {
    private int workItemId;
    private String title;
    private String workItemType;
    private String assignedToDisplayName;
    private String assignedToUniqueName; // For more precise user identification if needed
    private String state;
    private Instant changedDate;
    private Double completedWork;
    private String alertMessage;

    // Constructors
    public WorkItemTimeAlertDto() {}

    public WorkItemTimeAlertDto(
            int workItemId,
            String title,
            String workItemType,
            String assignedToDisplayName,
            String assignedToUniqueName,
            String state,
            Instant changedDate,
            Double completedWork,
            String alertMessage) {
        this.workItemId = workItemId;
        this.title = title;
        this.workItemType = workItemType;
        this.assignedToDisplayName = assignedToDisplayName;
        this.assignedToUniqueName = assignedToUniqueName;
        this.state = state;
        this.changedDate = changedDate;
        this.completedWork = completedWork;
        this.alertMessage = alertMessage;
    }

    // Standard Getters and Setters
    public int getWorkItemId() { return workItemId; }
    public void setWorkItemId(int workItemId) { this.workItemId = workItemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getWorkItemType() { return workItemType; }
    public void setWorkItemType(String workItemType) { this.workItemType = workItemType; }

    public String getAssignedToDisplayName() { return assignedToDisplayName; }
    public void setAssignedToDisplayName(String assignedToDisplayName) { this.assignedToDisplayName = assignedToDisplayName; }

    public String getAssignedToUniqueName() { return assignedToUniqueName; }
    public void setAssignedToUniqueName(String assignedToUniqueName) { this.assignedToUniqueName = assignedToUniqueName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Instant getChangedDate() { return changedDate; }
    public void setChangedDate(Instant changedDate) { this.changedDate = changedDate; }

    public Double getCompletedWork() { return completedWork; }
    public void setCompletedWork(Double completedWork) { this.completedWork = completedWork; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
}
