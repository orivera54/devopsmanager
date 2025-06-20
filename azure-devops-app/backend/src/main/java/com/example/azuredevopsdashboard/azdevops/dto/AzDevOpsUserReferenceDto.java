package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsUserReferenceDto {
    private String displayName;
    private String id;
    private String uniqueName;
    private String imageUrl; // URL to the user's avatar

    // Getters and Setters
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUniqueName() { return uniqueName; }
    public void setUniqueName(String uniqueName) { this.uniqueName = uniqueName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
