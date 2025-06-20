package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateWorkItemRequestDto {
    private String title;
    private String state;
    private String reason;
    private String assignedTo; // Could be email, displayName, or uniqueName. Service layer might need to resolve this.
    private String description; // Plain text description
    private Double originalEstimate;
    private Double completedWork;
    private Double remainingWork;
    private Integer priority;

    // For any other fields that are not explicitly mapped above.
    // Key should be the field reference name (e.g., "Custom.MyField").
    private Map<String, Object> customFields;

    // Default constructor for Jackson
    public UpdateWorkItemRequestDto() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getOriginalEstimate() { return originalEstimate; }
    public void setOriginalEstimate(Double originalEstimate) { this.originalEstimate = originalEstimate; }

    public Double getCompletedWork() { return completedWork; }
    public void setCompletedWork(Double completedWork) { this.completedWork = completedWork; }

    public Double getRemainingWork() { return remainingWork; }
    public void setRemainingWork(Double remainingWork) { this.remainingWork = remainingWork; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Map<String, Object> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, Object> customFields) { this.customFields = customFields; }
}
