package com.example.azuredevopsdashboard.azdevops.client;

import com.example.azuredevopsdashboard.azdevops.config.AzureDevOpsConfig;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectListResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemDto;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemListResponseDto;
import org.springframework.web.util.UriComponentsBuilder; // For URL construction
import java.util.List; // For List
import java.util.stream.Collectors; // For Collectors

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Service
public class AzureDevOpsClientService {

    private static final Logger logger = LoggerFactory.getLogger(AzureDevOpsClientService.class);

    private final RestTemplate restTemplate;
    private final AzureDevOpsConfig azureDevOpsConfig;

    @Autowired
    public AzureDevOpsClientService(RestTemplate restTemplate, AzureDevOpsConfig azureDevOpsConfig) {
        this.restTemplate = restTemplate;
        this.azureDevOpsConfig = azureDevOpsConfig;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String pat = azureDevOpsConfig.getPersonalAccessToken();
        if (StringUtils.hasText(pat)) {
            String auth = ":" + pat; // Username is empty for PAT
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        } else {
            logger.warn("Azure DevOps PAT is not configured. Requests to Azure DevOps will likely fail.");
            // Depending on Azure DevOps instance configuration, unauthenticated requests might still return some public data
            // or, more likely, fail. For this example, we proceed without Authorization header if PAT is missing.
        }
        return headers;
    }

    public AzDevOpsProjectListResponseDto getProjects() {
        if (!StringUtils.hasText(azureDevOpsConfig.getOrganizationUrl())) {
            logger.error("Azure DevOps Organization URL is not configured. Cannot fetch projects.");
            return null; // Or throw a custom configuration exception
        }
        // PAT absence is handled in createHeaders, but you might want to check here too if it's strictly required

        String url = String.format("%s/_apis/projects?api-version=%s",
                                   azureDevOpsConfig.getOrganizationUrl(),
                                   azureDevOpsConfig.getApiVersion());
        logger.info("Fetching projects from Azure DevOps URL: {}", url);

        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<AzDevOpsProjectListResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AzDevOpsProjectListResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                 logger.debug("Successfully fetched projects. Count: {}", response.getBody() != null ? response.getBody().getCount() : "N/A");
                return response.getBody();
            } else {
                logger.error("Failed to fetch projects. Status code: {}", response.getStatusCode());
                return null;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error fetching projects from Azure DevOps: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error fetching projects from Azure DevOps: {}", e.getMessage(), e);
            return null;
        }
    }

    public AzDevOpsWorkItemDto getWorkItem(String projectOrTeamName, int id, String expand) {
        if (!StringUtils.hasText(azureDevOpsConfig.getOrganizationUrl())) {
            logger.error("Azure DevOps Organization URL is not configured. Cannot fetch work item.");
            return null;
        }
        if (!StringUtils.hasText(projectOrTeamName)) {
            logger.error("Project or Team name is not provided. Cannot fetch work item.");
            return null;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(azureDevOpsConfig.getOrganizationUrl())
                .pathSegment(projectOrTeamName, "_apis", "wit", "workitems", String.valueOf(id))
                .queryParam("api-version", azureDevOpsConfig.getApiVersion());

        if (StringUtils.hasText(expand)) {
            builder.queryParam("$expand", expand); // e.g., "relations", "fields", "all"
        }

        String url = builder.toUriString();
        logger.info("Fetching work item from Azure DevOps URL: {}", url);

        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<AzDevOpsWorkItemDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AzDevOpsWorkItemDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Successfully fetched work item ID: {}", id);
                return response.getBody();
            } else {
                logger.error("Failed to fetch work item ID: {}. Status code: {}", id, response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error fetching work item ID {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error fetching work item ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    public AzDevOpsWorkItemListResponseDto getWorkItemsBatch(List<Integer> ids, String projectOrTeamName, String expand) {
        if (!StringUtils.hasText(azureDevOpsConfig.getOrganizationUrl())) {
            logger.error("Azure DevOps Organization URL is not configured. Cannot fetch work items batch.");
            return null;
        }
         if (!StringUtils.hasText(projectOrTeamName)) { // projectOrTeamName can be optional for org-level queries for some APIs, but usually required for work items
            logger.error("Project or Team name is not provided for batch work item fetch. This might be required depending on API version/endpoint.");
            // return null; // Or proceed if API allows org-level queries for workitemsbatch
        }
        if (ids == null || ids.isEmpty()) {
            logger.warn("Work item ID list is empty. Not fetching batch.");
            return new AzDevOpsWorkItemListResponseDto(); // Return empty response
        }

        // The /_apis/wit/workitemsbatch endpoint usually does not require a project context in the path,
        // but it's good practice to be aware that some APIs might behave differently or have project-specific versions.
        // For a specific project: /{project}/_apis/wit/workitemsbatch
        // For organization-level: /_apis/wit/workitemsbatch (if supported, or if project is implied by PAT scope)
        // For this example, let's assume an org-level endpoint or project context is handled by PAT scope or is not strictly needed in path.
        // If project context is needed in path: .pathSegment(projectOrTeamName, "_apis", "wit", "workitemsbatch")

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(azureDevOpsConfig.getOrganizationUrl())
                .pathSegment("_apis", "wit", "workitemsbatch") // Org-level, or adjust if project is needed in path
                .queryParam("api-version", azureDevOpsConfig.getApiVersion());

        if (StringUtils.hasText(expand)) {
            builder.queryParam("$expand", expand);
        }

        // Construct the request body for POST
        // The body should be a JSON object like: { "ids": [1, 2, 3], "fields": ["System.Id", "System.Title"] }
        // For simplicity, we'll fetch all default fields by not specifying "fields" parameter in body,
        // relying on $expand for more details.
        // If specific fields are needed, this body needs to be constructed.
        // String fieldsToFetch = "System.Id,System.Title,System.WorkItemType,System.State,System.AssignedTo";
        // builder.queryParam("fields", fieldsToFetch); // Alternative: specify fields as query param

        String url = builder.toUriString();
        logger.info("Fetching work items batch from Azure DevOps URL: {}", url);

        // Create request body
        String idsString = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        // The API expects a JSON body for POST: {"ids": [1,2,3], "$expand": "relations", "fields": ["System.Title", ...]}
        // Simpler version for GET-like behavior through POST (if API supports it by just sending IDs):
        // List<Integer> requestBodyIds = ids;
        // Or a map for full control:
        java.util.Map<String, Object> requestBodyMap = new java.util.HashMap<>();
        requestBodyMap.put("ids", ids);
        if (StringUtils.hasText(expand)) { // The $expand can also be part of the body for some AzDO POST APIs
             // requestBodyMap.put("$expand", expand); // This is less common for workitemsbatch, usually a query param
        }
        // requestBodyMap.put("fields", List.of("System.Id", "System.Title", "System.State")); // Example specific fields


        try {
            // HttpEntity<List<Integer>> entity = new HttpEntity<>(requestBodyIds, createHeaders()); // Simpler body
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(requestBodyMap, createHeaders());

            ResponseEntity<AzDevOpsWorkItemListResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST, // workitemsbatch is a POST request
                    entity,
                    AzDevOpsWorkItemListResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Successfully fetched work items batch. Count: {}", response.getBody() != null ? response.getBody().getCount() : "N/A");
                return response.getBody();
            } else {
                logger.error("Failed to fetch work items batch. Status code: {}", response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error fetching work items batch: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error fetching work items batch: {}", e.getMessage(), e);
            return null;
        }
    }

    public com.example.azuredevopsdashboard.azdevops.dto.WiqlResponseDto executeWiqlQuery(String projectOrTeamName, String wiqlQuery) {
        if (!StringUtils.hasText(projectOrTeamName)) {
            logger.error("Project or team name is required for WIQL query.");
            return null;
        }
        if (!StringUtils.hasText(wiqlQuery)) {
            logger.error("WIQL query string cannot be empty.");
            return null;
        }

        // Ensure organization URL is configured
        if (!StringUtils.hasText(azureDevOpsConfig.getOrganizationUrl())) {
            logger.error("Azure DevOps Organization URL is not configured. Cannot execute WIQL query.");
            return null;
        }

        String url = String.format("%s/%s/_apis/wit/wiql?api-version=%s",
                                   azureDevOpsConfig.getOrganizationUrl(),
                                   projectOrTeamName,
                                   azureDevOpsConfig.getApiVersion());

        logger.info("Executing WIQL query on URL: {}. Query: {}", url, wiqlQuery);
        com.example.azuredevopsdashboard.azdevops.dto.WiqlRequestDto requestBody = new com.example.azuredevopsdashboard.azdevops.dto.WiqlRequestDto(wiqlQuery);
        HttpEntity<com.example.azuredevopsdashboard.azdevops.dto.WiqlRequestDto> entity = new HttpEntity<>(requestBody, createHeaders());

        try {
            ResponseEntity<com.example.azuredevopsdashboard.azdevops.dto.WiqlResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    com.example.azuredevopsdashboard.azdevops.dto.WiqlResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.debug("WIQL query execution successful. Query Type: {}, Result Type: {}, AsOf: {}, WorkItems Found: {}",
                    response.getBody().getQueryType(),
                    response.getBody().getQueryResultType(),
                    response.getBody().getAsOf(),
                    response.getBody().getWorkItems() != null ? response.getBody().getWorkItems().size() : 0);
                return response.getBody();
            } else {
                 logger.error("WIQL query execution failed or returned empty body. Status: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Client error executing WIQL query for project {}: {} - {}", projectOrTeamName, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            logger.error("Server error executing WIQL query for project {}: {} - {}", projectOrTeamName, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Unexpected error executing WIQL query for project {}: {}", projectOrTeamName, e.getMessage(), e);
        }
        return null;
    }

    // Helper method to create headers specific for JSON Patch operations
    private HttpHeaders createJsonPatchHeaders() {
        HttpHeaders headers = createHeaders(); // Reuses base authentication and other common headers
        headers.setContentType(MediaType.valueOf("application/json-patch+json"));
        return headers;
    }

    public com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemDto updateWorkItem(int id, List<com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto> patchOperations) {
        if (!StringUtils.hasText(azureDevOpsConfig.getOrganizationUrl())) {
            logger.error("Azure DevOps Organization URL is not configured. Cannot update work item.");
            return null;
        }
        if (patchOperations == null || patchOperations.isEmpty()) {
            logger.warn("Patch operations list is empty for work item ID {}. No update will be performed.", id);
            // Optionally, one might fetch and return the work item as is, or return null/error.
            // For now, returning null as no update was attempted.
            return getWorkItem(null, id, "All"); // Return current state if no operations
        }

        // The Azure DevOps API for updating work items typically does not include project in the URL path for the PATCH operation itself.
        // The work item ID is unique across the organization.
        String url = String.format("%s/_apis/wit/workitems/%d?api-version=%s",
                                   azureDevOpsConfig.getOrganizationUrl(),
                                   id,
                                   azureDevOpsConfig.getApiVersion());

        logger.info("Updating work item with ID {} on URL: {}. Operations: {}", id, url, patchOperations);
        HttpEntity<List<com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto>> entity =
                new HttpEntity<>(patchOperations, createJsonPatchHeaders());

        try {
            ResponseEntity<com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH, // HTTP PATCH method for updates
                    entity,
                    com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Work item {} updated successfully. Title: {}", id, response.getBody().getFields() != null ? response.getBody().getFields().getTitle() : "N/A");
                return response.getBody();
            } else {
                logger.error("Work item {} update failed or returned empty body. Status: {}", id, response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Client error updating work item {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            logger.error("Server error updating work item {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Unexpected error updating work item {}: {}", id, e.getMessage(), e);
        }
        return null; // Or throw a specific exception
    }

    public com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemCommentListResponseDto getWorkItemComments(int workItemId, String expand) {
        if (!StringUtils.hasText(azureDevOpsConfig.getOrganizationUrl())) {
            logger.error("Azure DevOps Organization URL is not configured. Cannot fetch comments for work item ID {}.", workItemId);
            return null;
        }

        // The project is not needed in the URL path for fetching comments by work item ID.
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(azureDevOpsConfig.getOrganizationUrl())
                .pathSegment("_apis", "wit", "workItems", String.valueOf(workItemId), "comments")
                .queryParam("api-version", azureDevOpsConfig.getApiVersion()); // Consider a specific API version for comments if different

        if (StringUtils.hasText(expand)) {
            builder.queryParam("$expand", expand); // e.g., "renderedText" or "none" (default)
        }

        String url = builder.toUriString();
        logger.info("Fetching comments for work item ID {} from URL: {}", workItemId, url);
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemCommentListResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemCommentListResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                 logger.debug("Successfully fetched {} comments for work item {}. Total comments: {}",
                    response.getBody().getCount(), workItemId, response.getBody().getTotalCount());
                return response.getBody();
            } else {
                logger.error("Failed to fetch comments for work item {}. Status: {}", workItemId, response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Client error fetching comments for work item {}: {} - {}", workItemId, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            logger.error("Server error fetching comments for work item {}: {} - {}", workItemId, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching comments for work item {}: {}", workItemId, e.getMessage(), e);
        }
        return null;
    }
}
