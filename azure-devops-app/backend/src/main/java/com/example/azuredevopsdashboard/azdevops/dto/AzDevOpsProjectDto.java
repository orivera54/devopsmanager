package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant; // Using Instant for timestamps

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsProjectDto {
    private String id;
    private String name;
    private String description;
    private String url;
    private String state;
    private Instant lastUpdateTime; // Changed to Instant for better time handling

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Instant getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(Instant lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
}
