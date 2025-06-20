package com.example.azuredevopsdashboard.azdevops.controller;

import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectDto;
import com.example.azuredevopsdashboard.azdevops.service.AzDevOpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectDto; // Keep this import
import org.slf4j.Logger; // Keep this import
import org.slf4j.LoggerFactory; // Keep this import
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus; // For HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // For @PathVariable, @RequestParam

import java.util.Collections; // For Collections.emptyList()
import java.util.List;
import java.util.Optional; // For Optional

@RestController
@RequestMapping("/api/azdevops")
public class AzDevOpsController {

    private static final Logger logger = LoggerFactory.getLogger(AzDevOpsController.class);
    private final AzDevOpsService azDevOpsService;

    @Autowired
    public AzDevOpsController(AzDevOpsService azDevOpsService) {
        this.azDevOpsService = azDevOpsService;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<AzDevOpsProjectDto>> getProjects() {
        logger.info("Received request for listing Azure DevOps projects.");
        try {
            List<AzDevOpsProjectDto> projects = azDevOpsService.listProjects();
            // No need to explicitly check for empty list here, service layer handles it.
            // Controller's job is to translate service response to HTTP response.
            logger.info("Returning {} Azure DevOps projects.", projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving Azure DevOps projects.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()); // Return empty list on error
        }
    }

    @GetMapping("/workitems/{id}")
    public ResponseEntity<AzDevOpsWorkItemDto> getWorkItemById(
            @PathVariable int id,
            @RequestParam(required = false) String project,
            @RequestParam(required = false, defaultValue = "All") String expand) {

        logger.info("Received request for work item ID: {}, project: '{}', expand: '{}'", id, project, expand);
        try {
            Optional<AzDevOpsWorkItemDto> workItemOptional = azDevOpsService.getWorkItemById(project, id, expand);
            return workItemOptional
                    .map(workItem -> {
                        logger.info("Work item ID {} found.", id);
                        return ResponseEntity.ok(workItem);
                    })
                    .orElseGet(() -> {
                        logger.warn("Work item ID {} not found for project: '{}'.", id, project);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving work item ID {}.", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Using POST for batch is generally better if the number of IDs can be large,
    // as GET request URL length can be a limitation.
    // However, sticking to GET as per plan, but a POST version would look like:
    // @PostMapping("/workitems/batch")
    // public ResponseEntity<List<AzDevOpsWorkItemDto>> getWorkItemsByIdsPost(
    // @RequestBody WorkItemBatchRequestDto requestBody) { ... }
    // For GET with list of IDs:
    @GetMapping("/workitems/batch")
    public ResponseEntity<List<AzDevOpsWorkItemDto>> getWorkItemsByIds(
            @RequestParam(name = "ids") List<Integer> ids, // Ensure Spring can bind comma-separated string to List<Integer>
            @RequestParam(required = false) String project,
            @RequestParam(required = false, defaultValue = "All") String expand) {

        logger.info("Received request for work item IDs: {}, project: '{}', expand: '{}'", ids, project, expand);
        if (ids == null || ids.isEmpty()) {
            logger.warn("Work item ID list is empty in request.");
            return ResponseEntity.badRequest().body(Collections.emptyList()); // Or a specific error message
        }

        try {
            List<AzDevOpsWorkItemDto> workItems = azDevOpsService.getWorkItemsByIds(project, ids, expand);
            // Even if workItems is empty, it's a valid response if some IDs didn't exist or had no access
            logger.info("Returning {} work items for batch request.", workItems.size());
            return ResponseEntity.ok(workItems);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving work items by batch.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping("/projects/{projectOrTeamName}/workitems/query")
    public ResponseEntity<List<AzDevOpsWorkItemDto>> queryWorkItems(
            @PathVariable String projectOrTeamName,
            @RequestBody com.example.azuredevopsdashboard.azdevops.dto.WorkItemQueryRequestDto queryRequest,
            @RequestParam(required = false, defaultValue = "All") String expand) {

        if (queryRequest == null || !org.springframework.util.StringUtils.hasText(queryRequest.getWiql())) {
            logger.warn("WIQL query request is null or query string is empty for project: {}", projectOrTeamName);
            return ResponseEntity.badRequest().build();
        }
        if (!org.springframework.util.StringUtils.hasText(projectOrTeamName)) {
             logger.warn("Project/Team name path variable is missing.");
             return ResponseEntity.badRequest().build(); // Project/Team is necessary
        }

        logger.info("Received WIQL query for project/team '{}', expand: '{}'. Query: [{}]",
            projectOrTeamName, expand, queryRequest.getWiql());

        try {
            List<AzDevOpsWorkItemDto> workItems = azDevOpsService.getWorkItemsByWiql(projectOrTeamName, queryRequest.getWiql(), expand);
            logger.info("Returning {} work items from WIQL query for project/team '{}'.", workItems.size(), projectOrTeamName);
            return ResponseEntity.ok(workItems);
        } catch (Exception e) {
            logger.error("Error processing WIQL query for project {}: {}", projectOrTeamName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/workitems/{id}")
    public ResponseEntity<AzDevOpsWorkItemDto> updateWorkItem(
            @PathVariable int id,
            @RequestBody com.example.azuredevopsdashboard.azdevops.dto.UpdateWorkItemRequestDto updateRequest) {

        if (updateRequest == null) {
            logger.warn("Update request for work item ID {} is null.", id);
            return ResponseEntity.badRequest().build();
        }

        logger.info("Received request to update work item ID: {}. Request details: {}", id, updateRequest);

        try {
            // The AzDevOpsService.updateWorkItemDetails now returns Optional<AzDevOpsWorkItemDto>
            Optional<AzDevOpsWorkItemDto> updatedWorkItemOptional = azDevOpsService.updateWorkItemDetails(id, updateRequest);

            return updatedWorkItemOptional
                    .map(workItem -> {
                        logger.info("Successfully updated work item ID: {}.", id);
                        return ResponseEntity.ok(workItem);
                    })
                    .orElseGet(() -> {
                        // This case could mean the work item was not found for update,
                        // or no actual update was performed (e.g. no operations),
                        // or an error occurred in the client/service layer that resulted in null.
                        // The service layer logs specifics. Controller can return a generic server error or more specific if info is available.
                        logger.error("Failed to update work item ID: {}, or no update was performed, or work item not found after update attempt.", id);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        // Or consider: ResponseEntity.notFound().build(); if it's certain the item doesn't exist
                        // Or if no operations: ResponseEntity.ok(originalWorkItemFetchedInService);
                    });
        } catch (IllegalArgumentException e) { // Catching potential specific exceptions from service
            logger.warn("Bad request for updating work item {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e) { // General catch-all for other unexpected errors
            logger.error("Error updating work item {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/workitems/{workItemId}/comments")
    public ResponseEntity<List<com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemCommentDto>> getWorkItemComments(
            @PathVariable int workItemId,
            @RequestParam(required = false, defaultValue = "None") String expand // Default to "None" or "renderedText"
    ) {
        logger.info("Received request for comments of work item ID: {}, expand: '{}'", workItemId, expand);
        try {
            List<com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemCommentDto> comments =
                azDevOpsService.listWorkItemComments(workItemId, expand.equalsIgnoreCase("None") ? null : expand);

            // The service layer already returns an empty list if no comments are found or an error occurs,
            // so we can directly return that.
            logger.info("Returning {} comments for work item ID: {}", comments.size(), workItemId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            // This generic catch is for unexpected errors in the controller or service layer above client.
            logger.error("Error fetching comments for work item {}: {}", workItemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
