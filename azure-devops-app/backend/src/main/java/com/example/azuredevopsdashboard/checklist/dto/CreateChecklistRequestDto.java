package com.example.azuredevopsdashboard.checklist.dto;

import java.util.List;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Size;
// import jakarta.validation.Valid; // For nested validation of items

public class CreateChecklistRequestDto {

    // @NotBlank(message = "Checklist name cannot be blank")
    // @Size(max = 255, message = "Checklist name cannot exceed 255 characters")
    private String name;

    private String description;

    // @NotBlank(message = "Azure DevOps organization cannot be blank")
    private String azureDevopsOrganization;

    // @NotBlank(message = "Azure DevOps project name cannot be blank")
    private String azureDevopsProjectName;

    // @NotNull(message = "Azure DevOps work item ID cannot be null")
    private Integer azureDevopsWorkItemId;

    // @Valid // If items list should be validated as well
    private List<CreateChecklistItemDto> items; // For creating items along with the checklist

    // Default constructor for Jackson
    public CreateChecklistRequestDto() {}

    // Getters and Setters
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

    public List<CreateChecklistItemDto> getItems() { return items; }
    public void setItems(List<CreateChecklistItemDto> items) { this.items = items; }
}
