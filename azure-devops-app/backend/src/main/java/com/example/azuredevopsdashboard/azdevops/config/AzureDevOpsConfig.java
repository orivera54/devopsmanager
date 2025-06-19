package com.example.azuredevopsdashboard.azdevops.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AzureDevOpsConfig {

    @Value("${azure.devops.organization.url:}") // Default to empty if not set
    private String organizationUrl;

    @Value("${azure.devops.pat:}") // Default to empty if not set
    private String personalAccessToken;

    @Value("${azure.devops.api.version:7.1}") // Default version
    private String apiVersion;

    // Getters
    public String getOrganizationUrl() { return organizationUrl; }
    public String getPersonalAccessToken() { return personalAccessToken; }
    public String getApiVersion() { return apiVersion; }

    // Optional: Add setters if you need to modify them post-construction,
    // or remove them if properties are meant to be immutable after injection.
    public void setOrganizationUrl(String organizationUrl) { this.organizationUrl = organizationUrl; }
    public void setPersonalAccessToken(String personalAccessToken) { this.personalAccessToken = personalAccessToken; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
}
