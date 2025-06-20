package com.example.azuredevopsdashboard.checklist.dto;

import java.time.Instant;
import java.util.List;

public class ChecklistDto {
    private Long id;
    private String name;
    private String description;
    private String azureDevopsOrganization;
    private String azureDevopsProjectName;
    private Integer azureDevopsWorkItemId;
    private Integer createdByUserId; // Changed to Integer to match User.id type
    private String createdByUsername;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ChecklistItemDto> items;

    // Default constructor
    public ChecklistDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAzureDevopsOrganization() { return azureDevopsOrganization; }
    public void setAzureDevopsOrganization(String azureDevopsOrganization) { this.azureDevopsOrganization = azureDevopsOrganization; }

    public String getAzureDevopsProjectName() { return azureDevopsProjectName; }
    public void setAzureDevopsProjectName(String azureDevopsProjectName) { this.azureDevopsProjectName = azureDevopsProjectName; }

    public Integer getAzureDevopsWorkItemId() { return azureDevopsWorkItemId; }
    public void setAzureDevopsWorkItemId(Integer azureDevopsWorkItemId) { this.azureDevopsWorkItemId = azureDevopsWorkItemId; }

    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ChecklistItemDto> getItems() { return items; }
    public void setItems(List<ChecklistItemDto> items) { this.items = items; }
}
