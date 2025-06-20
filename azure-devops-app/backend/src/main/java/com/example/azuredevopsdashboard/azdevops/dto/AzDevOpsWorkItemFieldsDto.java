package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsWorkItemFieldsDto {

    @JsonProperty("System.TeamProject")
    private String teamProject;

    @JsonProperty("System.WorkItemType")
    private String workItemType;

    @JsonProperty("System.Title")
    private String title;

    @JsonProperty("System.State")
    private String state;

    @JsonProperty("System.Reason")
    private String reason;

    @JsonProperty("System.AssignedTo")
    private AzDevOpsUserReferenceDto assignedTo;

    @JsonProperty("System.CreatedDate")
    private Instant createdDate;

    @JsonProperty("System.CreatedBy")
    private AzDevOpsUserReferenceDto createdBy;

    @JsonProperty("System.ChangedDate")
    private Instant changedDate;

    @JsonProperty("System.ChangedBy")
    private AzDevOpsUserReferenceDto changedBy;

    @JsonProperty("System.Description") // For plain text description
    private String description;

    @JsonProperty("System.DescriptionHtml") // For HTML description
    private String descriptionHtml;


    @JsonProperty("Microsoft.VSTS.Common.Priority")
    private Integer priority;

    @JsonProperty("Microsoft.VSTS.Scheduling.OriginalEstimate")
    private Double originalEstimate; // Estimated hours

    @JsonProperty("Microsoft.VSTS.Scheduling.CompletedWork")
    private Double completedWork; // Completed hours

    @JsonProperty("Microsoft.VSTS.Scheduling.RemainingWork")
    private Double remainingWork; // Remaining hours

    @JsonProperty("System.IterationPath")
    private String iterationPath;

    @JsonProperty("System.AreaPath")
    private String areaPath;

    // Getters and Setters
    public String getTeamProject() { return teamProject; }
    public void setTeamProject(String teamProject) { this.teamProject = teamProject; }

    public String getWorkItemType() { return workItemType; }
    public void setWorkItemType(String workItemType) { this.workItemType = workItemType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public AzDevOpsUserReferenceDto getAssignedTo() { return assignedTo; }
    public void setAssignedTo(AzDevOpsUserReferenceDto assignedTo) { this.assignedTo = assignedTo; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public AzDevOpsUserReferenceDto getCreatedBy() { return createdBy; }
    public void setCreatedBy(AzDevOpsUserReferenceDto createdBy) { this.createdBy = createdBy; }

    public Instant getChangedDate() { return changedDate; }
    public void setChangedDate(Instant changedDate) { this.changedDate = changedDate; }

    public AzDevOpsUserReferenceDto getChangedBy() { return changedBy; }
    public void setChangedBy(AzDevOpsUserReferenceDto changedBy) { this.changedBy = changedBy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionHtml() { return descriptionHtml; }
    public void setDescriptionHtml(String descriptionHtml) { this.descriptionHtml = descriptionHtml; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Double getOriginalEstimate() { return originalEstimate; }
    public void setOriginalEstimate(Double originalEstimate) { this.originalEstimate = originalEstimate; }

    public Double getCompletedWork() { return completedWork; }
    public void setCompletedWork(Double completedWork) { this.completedWork = completedWork; }

    public Double getRemainingWork() { return remainingWork; }
    public void setRemainingWork(Double remainingWork) { this.remainingWork = remainingWork; }

    public String getIterationPath() { return iterationPath; }
    public void setIterationPath(String iterationPath) { this.iterationPath = iterationPath; }

    public String getAreaPath() { return areaPath; }
    public void setAreaPath(String areaPath) { this.areaPath = areaPath; }
}
