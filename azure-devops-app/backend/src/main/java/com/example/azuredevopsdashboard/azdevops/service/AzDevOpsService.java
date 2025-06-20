package com.example.azuredevopsdashboard.azdevops.service;

import com.example.azuredevopsdashboard.azdevops.client.AzureDevOpsClientService;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectDto;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectListResponseDto;
import com.example.azuredevopsdashboard.azdevops.config.AzureDevOpsConfig; // Ensure this is imported
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemDto; // Ensure this is imported
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // For StringUtils.hasText

import java.util.Collections;
import java.util.List;
import java.util.Optional; // For Optional return type

@Service
public class AzDevOpsService {

    private static final Logger logger = LoggerFactory.getLogger(AzDevOpsService.class);
    private final AzureDevOpsClientService azureDevOpsClientService;
    private final AzureDevOpsConfig azureDevOpsConfig; // Added for default project/org access

    @Autowired
    public AzDevOpsService(AzureDevOpsClientService azureDevOpsClientService, AzureDevOpsConfig azureDevOpsConfig) {
        this.azureDevOpsClientService = azureDevOpsClientService;
        this.azureDevOpsConfig = azureDevOpsConfig; // Injected
    }

    public List<AzDevOpsProjectDto> listProjects() {
        logger.debug("Attempting to list Azure DevOps projects via AzDevOpsService.");
        AzDevOpsProjectListResponseDto response = azureDevOpsClientService.getProjects();

        if (response != null && response.getValue() != null) {
            logger.info("Successfully fetched {} projects from Azure DevOps.", response.getValue().size());
            return response.getValue();
        }

        logger.warn("No projects found or an error occurred while fetching from Azure DevOps through client service.");
        return Collections.emptyList();
    }

    public Optional<AzDevOpsWorkItemDto> getWorkItemById(String projectOrTeamName, int id, String expandLevel) {
        String project = StringUtils.hasText(projectOrTeamName) ? projectOrTeamName : getDefaultProjectNameFromConfig();
        if (!StringUtils.hasText(project)) {
             logger.error("Project name is required to fetch a work item but was not provided and could not be derived from config.");
             return Optional.empty();
        }
        logger.info("Fetching work item with ID {} from project/team '{}', expand level: '{}'", id, project, expandLevel);
        AzDevOpsWorkItemDto workItem = azureDevOpsClientService.getWorkItem(project, id, expandLevel);
        if (workItem == null) {
            logger.warn("Work item with ID {} not found in project/team '{}'", id, project);
        }
        return Optional.ofNullable(workItem);
    }

    public List<AzDevOpsWorkItemDto> getWorkItemsByIds(String projectOrTeamName, List<Integer> ids, String expandLevel) {
        if (ids == null || ids.isEmpty()) {
            logger.warn("Work item ID list is empty for batch fetch, returning empty list.");
            return Collections.emptyList();
        }
        String project = StringUtils.hasText(projectOrTeamName) ? projectOrTeamName : getDefaultProjectNameFromConfig();
         if (!StringUtils.hasText(project)) {
             logger.error("Project name is required to fetch work items by batch but was not provided and could not be derived from config.");
             return Collections.emptyList();
        }
        logger.info("Fetching {} work items by IDs from project/team '{}', expand level: '{}'", ids.size(), project, expandLevel);
        AzDevOpsWorkItemListResponseDto response = azureDevOpsClientService.getWorkItemsBatch(ids, project, expandLevel);
        if (response != null && response.getValue() != null) {
            logger.info("Successfully fetched {} work items in batch.", response.getValue().size());
            return response.getValue();
        }
        logger.warn("No work items found or error occurred during batch fetch for IDs: {}", ids);
        return Collections.emptyList();
    }

    // Helper to attempt to get a default project name.
    // This is a simplified helper. In a real app, project context might be more complex.
    private String getDefaultProjectNameFromConfig() {
        // This example assumes the "organizationUrl" might sometimes contain the project,
        // e.g., https://dev.azure.com/MyOrg/MyProject. This is not standard for all AzDO APIs.
        // A more robust way is to have a separate property like 'azure.devops.default.project'
        // or require the project to be explicitly passed.
        String orgUrl = azureDevOpsConfig.getOrganizationUrl();
        if (orgUrl == null) return null;

        try {
            String path = new java.net.URL(orgUrl).getPath(); // e.g., /MyOrg/MyProject or /MyOrg
            String[] parts = path.split("/");
            if (parts.length > 2 && StringUtils.hasText(parts[2])) { // parts[0]="", parts[1]="MyOrg", parts[2]="MyProject"
                logger.debug("Derived default project name '{}' from organization URL.", parts[2]);
                return parts[2];
            } else {
                 logger.debug("Organization URL '{}' does not seem to contain a default project in its path.", orgUrl);
            }
        } catch (java.net.MalformedURLException e) {
            logger.error("Invalid organization URL format in config: {}", orgUrl, e);
        }
        // If no project in URL path, or if it's preferred to have an explicit default project property:
        // String defaultProject = azureDevOpsConfig.getDefaultProject(); // Assuming a getter for such a property
        // if (StringUtils.hasText(defaultProject)) return defaultProject;

        // String defaultProject = azureDevOpsConfig.getDefaultProject(); // Assuming a getter for such a property
        // if (StringUtils.hasText(defaultProject)) return defaultProject;

        return null; // No default project could be determined
    }

    public List<AzDevOpsWorkItemDto> getWorkItemsByWiql(String projectOrTeamName, String wiqlQuery, String expandLevel) {
        String effectiveProjectName = StringUtils.hasText(projectOrTeamName) ? projectOrTeamName : getDefaultProjectNameFromConfig();
        if (!StringUtils.hasText(effectiveProjectName)) {
            logger.error("Project or team name is required to execute WIQL query and could not be derived.");
            return Collections.emptyList();
        }
        if (!StringUtils.hasText(wiqlQuery)) {
            logger.error("WIQL query string cannot be empty for project/team '{}'.", effectiveProjectName);
            return Collections.emptyList();
        }

        logger.info("Executing WIQL query for project/team '{}': [{}], expand level: '{}'", effectiveProjectName, wiqlQuery, expandLevel);
        com.example.azuredevopsdashboard.azdevops.dto.WiqlResponseDto wiqlResponse = azureDevOpsClientService.executeWiqlQuery(effectiveProjectName, wiqlQuery);

        if (wiqlResponse == null || wiqlResponse.getWorkItems() == null || wiqlResponse.getWorkItems().isEmpty()) {
            logger.warn("WIQL query for project/team '{}' returned no work item references or an error occurred. Query: [{}]", effectiveProjectName, wiqlQuery);
            return Collections.emptyList();
        }

        List<Integer> workItemIds = wiqlResponse.getWorkItems().stream()
                                                .map(com.example.azuredevopsdashboard.azdevops.dto.WiqlWorkItemReferenceDto::getId)
                                                .collect(java.util.stream.Collectors.toList());

        if (workItemIds.isEmpty()) {
            logger.info("WIQL query for project/team '{}' returned references, but no IDs could be extracted. Query: [{}]", effectiveProjectName, wiqlQuery);
            return Collections.emptyList();
        }

        logger.info("WIQL query for project/team '{}' successful, found {} work item IDs. Fetching details with expand level '{}'...", effectiveProjectName, workItemIds.size(), expandLevel);
        // Use the same 'effectiveProjectName' for fetching batch details.
        return getWorkItemsByIds(effectiveProjectName, workItemIds, expandLevel);
    }

    public Optional<AzDevOpsWorkItemDto> updateWorkItemDetails(int workItemId, com.example.azuredevopsdashboard.azdevops.dto.UpdateWorkItemRequestDto updateRequest) {
        List<com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto> patchOperations = new ArrayList<>();

        // Standard fields
        if (StringUtils.hasText(updateRequest.getTitle())) {
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/System.Title", updateRequest.getTitle()));
        }
        if (StringUtils.hasText(updateRequest.getState())) {
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/System.State", updateRequest.getState()));
        }
        if (StringUtils.hasText(updateRequest.getReason())) {
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/System.Reason", updateRequest.getReason()));
        }

        // AssignedTo: This is complex. AzDO expects an IdentityRef object or sometimes just a display name.
        // A simple string might not always work or might work differently based on context.
        // If "" means unassign, then "op": "remove" might be needed.
        // For this example, we assume 'add' (which also means 'replace' if field exists) with the string value.
        if (updateRequest.getAssignedTo() != null) {
            if (updateRequest.getAssignedTo().isEmpty()) {
                 // To unassign, you typically set the value to null or use "remove" operation
                 patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/System.AssignedTo", null));
            } else {
                patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/System.AssignedTo", updateRequest.getAssignedTo()));
            }
        }

        if (StringUtils.hasText(updateRequest.getDescription())) {
            // For multi-line descriptions or HTML, ensure this is handled correctly by client and AzDO.
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/System.Description", updateRequest.getDescription()));
        }

        // Scheduling fields
        if (updateRequest.getOriginalEstimate() != null) {
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/Microsoft.VSTS.Scheduling.OriginalEstimate", updateRequest.getOriginalEstimate()));
        }
        if (updateRequest.getCompletedWork() != null) {
            // Note: CompletedWork is often read-only and updated by specific actions (e.g. time tracking).
            // Explicitly setting it might be disallowed or have side effects.
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/Microsoft.VSTS.Scheduling.CompletedWork", updateRequest.getCompletedWork()));
        }
        if (updateRequest.getRemainingWork() != null) {
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/Microsoft.VSTS.Scheduling.RemainingWork", updateRequest.getRemainingWork()));
        }

        if (updateRequest.getPriority() != null) {
            patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/Microsoft.VSTS.Common.Priority", updateRequest.getPriority()));
        }

        // Custom fields from the map
        if (updateRequest.getCustomFields() != null) {
            for (java.util.Map.Entry<String, Object> entry : updateRequest.getCustomFields().entrySet()) {
                // Ensure the key is the full reference name, e.g., "Custom.MyCustomField"
                // The value type should match what AzDO expects for that field.
                patchOperations.add(new com.example.azuredevopsdashboard.azdevops.dto.patch.JsonPatchOperationDto("add", "/fields/" + entry.getKey(), entry.getValue()));
            }
        }

        if (patchOperations.isEmpty()) {
            logger.warn("No update operations constructed for work item ID: {}. Returning current state.", workItemId);
            // Fetch and return the work item in its current state if no operations are to be performed.
            // The project name for getWorkItemById might need to be determined or passed if not derivable.
            // Assuming 'null' for projectOrTeamName in getWorkItemById will trigger its default project logic.
            return getWorkItemById(null, workItemId, "All");
        }

        logger.info("Attempting to update work item ID: {} with {} operations.", workItemId, patchOperations.size());
        AzDevOpsWorkItemDto updatedWorkItem = azureDevOpsClientService.updateWorkItem(workItemId, patchOperations);
        return Optional.ofNullable(updatedWorkItem);
    }

    public List<com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemCommentDto> listWorkItemComments(int workItemId, String expand) {
        logger.info("Listing comments for work item ID: {}, expand: '{}'", workItemId, expand);
        com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsWorkItemCommentListResponseDto response =
            azureDevOpsClientService.getWorkItemComments(workItemId, expand);

        if (response != null && response.getCommentList() != null) {
            logger.info("Successfully fetched {} comments for work item ID {}. (Total reported by API: {})",
                response.getCommentList().size(), workItemId, response.getTotalCount());
            return response.getCommentList();
        }

        logger.warn("No comments found or error occurred for work item ID {}.", workItemId);
        return Collections.emptyList();
    }

    public List<WorkItemTimeAlertDto> getTimeReportAlerts(String projectOrTeamName, List<String> statesToCheck, int daysSinceLastChangeThreshold) {
        String effectiveProjectName = StringUtils.hasText(projectOrTeamName) ? projectOrTeamName : getDefaultProjectNameFromConfig();
        if (!StringUtils.hasText(effectiveProjectName)) {
            logger.error("Project or team name is required for time report alerts and could not be derived.");
            return Collections.emptyList();
        }

        if (statesToCheck == null || statesToCheck.isEmpty()) {
            logger.warn("No states provided to check for time report alerts for project/team '{}'. Defaulting to 'Active'.", effectiveProjectName);
            statesToCheck = Collections.singletonList("Active");
        }

        String statesInQuery = statesToCheck.stream()
                                        .map(s -> "'" + s.replace("'", "''") + "'") // Escape single quotes
                                        .collect(java.util.stream.Collectors.joining(", "));

        // Note: WIQL @project refers to the project context of the API call, not a variable to be replaced here.
        // The effectiveProjectName is used in the service call to queryWorkItems which sets the project context for the API.
        String wiqlCriteria = String.format(
            "SELECT [System.Id], [System.Title], [System.WorkItemType], [System.AssignedTo], [System.State], [System.ChangedDate], [Microsoft.VSTS.Scheduling.CompletedWork] " +
            "FROM workitems " +
            "WHERE [System.State] IN (%s) " +
            "AND ([Microsoft.VSTS.Scheduling.CompletedWork] = null OR [Microsoft.VSTS.Scheduling.CompletedWork] = 0) " +
            "ORDER BY [System.ChangedDate] DESC",
            statesInQuery
        );

        // The daysSinceLastChangeThreshold logic is not yet incorporated into this WIQL.
        // It would require adding another condition like: AND [System.ChangedDate] < @today-%d
        // This is complex due to WIQL date functions and timezones.
        // For now, the primary alert is based on 'CompletedWork = 0 OR null' in active states.

        logger.info("Fetching work items for time report alerts for project/team '{}' using states: [{}]. WIQL: {}", effectiveProjectName, statesInQuery, wiqlCriteria);
        List<AzDevOpsWorkItemDto> workItems = getWorkItemsByWiql(effectiveProjectName, wiqlCriteria, "Fields"); // Reuses existing method

        return workItems.stream()
            .map(wi -> {
                String alertMsg = String.format(
                    "Work item %d ('%s') is in state '%s' with no (or zero) completed work reported.",
                    wi.getId(),
                    wi.getFields().getTitle(), // Assuming getTitle() exists and maps to "System.Title"
                    wi.getFields().getState()  // Assuming getState() exists and maps to "System.State"
                );
                return new WorkItemTimeAlertDto(
                    wi.getId(),
                    wi.getFields().getTitle(),
                    wi.getFields().getWorkItemType(),
                    Optional.ofNullable(wi.getFields().getAssignedTo()).map(AzDevOpsUserReferenceDto::getDisplayName).orElse("Unassigned"),
                    Optional.ofNullable(wi.getFields().getAssignedTo()).map(AzDevOpsUserReferenceDto::getUniqueName).orElse(null),
                    wi.getFields().getState(),
                    wi.getFields().getChangedDate(),
                    wi.getFields().getCompletedWork(),
                    alertMsg
                );
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
