package com.example.azuredevopsdashboard.azdevops.service;

import com.example.azuredevopsdashboard.azdevops.client.AzureDevOpsClientService;
import com.example.azuredevopsdashboard.azdevops.config.AzureDevOpsConfig;
import com.example.azuredevopsdashboard.azdevops.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils; // For mocking StringUtils if needed, though not directly here

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList; // For new ArrayList<>()

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AzDevOpsServiceTest {

    @Mock
    private AzureDevOpsClientService mockClientService;
    @Mock
    private AzureDevOpsConfig mockConfig;

    @InjectMocks
    private AzDevOpsService azDevOpsService;

    private AzDevOpsProjectDto projectDto;
    private AzDevOpsWorkItemDto workItemDto;
    private AzDevOpsWorkItemFieldsDto workItemFieldsDto;

    @BeforeEach
    void setUp() {
        projectDto = new AzDevOpsProjectDto();
        projectDto.setId("project-id");
        projectDto.setName("Test Project");

        workItemDto = new AzDevOpsWorkItemDto();
        workItemDto.setId(1);
        workItemFieldsDto = new AzDevOpsWorkItemFieldsDto();
        workItemFieldsDto.setTitle("Test Work Item");
        workItemFieldsDto.setState("Active");
        workItemFieldsDto.setWorkItemType("Task");
        // Required for getTimeReportAlerts
        workItemFieldsDto.setCompletedWork(0.0);
        workItemFieldsDto.setChangedDate(Instant.now());
        AzDevOpsUserReferenceDto userRef = new AzDevOpsUserReferenceDto();
        userRef.setDisplayName("Test User");
        userRef.setUniqueName("test@user.com");
        workItemFieldsDto.setAssignedTo(userRef);
        workItemDto.setFields(workItemFieldsDto);

        // Common mocking for config if getDefaultProjectNameFromConfig is hit
        // Ensure a default behavior for org URL to avoid NPEs if that method is called.
        lenient().when(mockConfig.getOrganizationUrl()).thenReturn("https://dev.azure.com/org/defaultProject");
    }

    @Test
    void listProjects_shouldReturnProjectsFromClient() {
        AzDevOpsProjectListResponseDto clientResponse = new AzDevOpsProjectListResponseDto();
        clientResponse.setValue(Collections.singletonList(projectDto));
        clientResponse.setCount(1);
        when(mockClientService.getProjects()).thenReturn(clientResponse);

        List<AzDevOpsProjectDto> result = azDevOpsService.listProjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        verify(mockClientService).getProjects();
    }

    @Test
    void listProjects_shouldReturnEmptyListWhenClientReturnsNull() {
        when(mockClientService.getProjects()).thenReturn(null);
        List<AzDevOpsProjectDto> result = azDevOpsService.listProjects();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listProjects_shouldReturnEmptyListWhenClientResponseValueIsNull() {
        AzDevOpsProjectListResponseDto clientResponse = new AzDevOpsProjectListResponseDto();
        clientResponse.setValue(null); // Value is null
        when(mockClientService.getProjects()).thenReturn(clientResponse);
        List<AzDevOpsProjectDto> result = azDevOpsService.listProjects();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void getWorkItemById_shouldReturnWorkItemFromClient_whenProjectProvided() {
        when(mockClientService.getWorkItem(eq("CustomProject"), eq(1), eq("All"))).thenReturn(workItemDto);

        Optional<AzDevOpsWorkItemDto> result = azDevOpsService.getWorkItemById("CustomProject", 1, "All");

        assertTrue(result.isPresent());
        assertEquals("Test Work Item", result.get().getFields().getTitle());
        verify(mockClientService).getWorkItem("CustomProject", 1, "All");
    }

    @Test
    void getWorkItemById_shouldUseDefaultProject_whenProjectNotProvided() {
        // This test relies on getDefaultProjectNameFromConfig() and its mock setup in @BeforeEach
        when(mockClientService.getWorkItem(eq("defaultProject"), eq(1), eq("All"))).thenReturn(workItemDto);

        Optional<AzDevOpsWorkItemDto> result = azDevOpsService.getWorkItemById(null, 1, "All");

        assertTrue(result.isPresent());
        assertEquals("Test Work Item", result.get().getFields().getTitle());
        verify(mockClientService).getWorkItem("defaultProject", 1, "All");
    }

    @Test
    void getWorkItemById_shouldReturnEmptyOptional_ifProjectCannotBeDetermined() {
        when(mockConfig.getOrganizationUrl()).thenReturn(null); // Make getDefaultProjectNameFromConfig return null
        Optional<AzDevOpsWorkItemDto> result = azDevOpsService.getWorkItemById(null, 1, "All");
        assertFalse(result.isPresent());
        verifyNoInteractions(mockClientService); // Client should not be called
    }


    @Test
    void getTimeReportAlerts_shouldGenerateAlertsForActiveItemsWithNoCompletedWork() {
        // Setup for the WIQL query part
        WiqlResponseDto wiqlResponse = new WiqlResponseDto();
        WiqlWorkItemReferenceDto refDto = new WiqlWorkItemReferenceDto();
        refDto.setId(workItemDto.getId()); // Use ID from setup
        wiqlResponse.setWorkItems(Collections.singletonList(refDto));

        // This is the WIQL string the service will build (simplified, without project name as it's implicit for the endpoint)
        // String expectedWiql = "SELECT [System.Id], [System.Title], [System.WorkItemType], [System.AssignedTo], [System.State], [System.ChangedDate], [Microsoft.VSTS.Scheduling.CompletedWork] FROM workitems WHERE [System.State] IN ('Active') AND ([Microsoft.VSTS.Scheduling.CompletedWork] = null OR [Microsoft.VSTS.Scheduling.CompletedWork] = 0) ORDER BY [System.ChangedDate] DESC";
        // The actual WIQL passed to client might vary based on how project name is handled.
        // For the test, we care that *a* WIQL string is passed and then the IDs are used.

        when(mockClientService.executeWiqlQuery(eq("TestProject"), anyString())).thenReturn(wiqlResponse);

        // Setup for the getWorkItemsByIds part (which is called by getWorkItemsByWiql)
        AzDevOpsWorkItemListResponseDto batchResponse = new AzDevOpsWorkItemListResponseDto();
        batchResponse.setValue(Collections.singletonList(workItemDto)); // Use workItem from setup
        batchResponse.setCount(1);
        when(mockClientService.getWorkItemsBatch(eq(Collections.singletonList(workItemDto.getId())), eq("TestProject"), eq("Fields"))).thenReturn(batchResponse);

        List<String> states = Collections.singletonList("Active");
        List<WorkItemTimeAlertDto> alerts = azDevOpsService.getTimeReportAlerts("TestProject", states, 2);

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        WorkItemTimeAlertDto alert = alerts.get(0);
        assertEquals(workItemDto.getId(), alert.getWorkItemId());
        assertEquals("Test Work Item", alert.getTitle());
        assertEquals("Task", alert.getWorkItemType());
        assertEquals("Test User", alert.getAssignedToDisplayName());
        assertEquals("test@user.com", alert.getAssignedToUniqueName());
        assertEquals("Active", alert.getState());
        assertEquals(0.0, alert.getCompletedWork());
        assertTrue(alert.getAlertMessage().contains("no (or zero) completed work reported"));

        verify(mockClientService).executeWiqlQuery(eq("TestProject"), anyString());
        verify(mockClientService).getWorkItemsBatch(eq(Collections.singletonList(workItemDto.getId())), eq("TestProject"), eq("Fields"));
    }

    @Test
    void getTimeReportAlerts_shouldReturnEmptyList_whenNoProjectName() {
        when(mockConfig.getOrganizationUrl()).thenReturn(null); // Ensure default project cannot be derived
        List<WorkItemTimeAlertDto> alerts = azDevOpsService.getTimeReportAlerts(null, Collections.singletonList("Active"), 2);
        assertTrue(alerts.isEmpty());
        verifyNoInteractions(mockClientService);
    }

    @Test
    void getTimeReportAlerts_shouldUseDefaultStates_whenStatesNotProvided() {
        // Similar setup to getTimeReportAlerts_shouldGenerateAlertsForActiveItemsWithNoCompletedWork
        // but pass null for statesToCheck
        WiqlResponseDto wiqlResponse = new WiqlResponseDto();
        WiqlWorkItemReferenceDto refDto = new WiqlWorkItemReferenceDto();
        refDto.setId(workItemDto.getId());
        wiqlResponse.setWorkItems(Collections.singletonList(refDto));

        AzDevOpsWorkItemListResponseDto batchResponse = new AzDevOpsWorkItemListResponseDto();
        batchResponse.setValue(Collections.singletonList(workItemDto));
        batchResponse.setCount(1);

        // WIQL will use default state 'Active'
        // String expectedWiqlForDefault = "SELECT [System.Id], [System.Title], [System.WorkItemType], [System.AssignedTo], [System.State], [System.ChangedDate], [Microsoft.VSTS.Scheduling.CompletedWork] FROM workitems WHERE [System.State] IN ('Active') AND ([Microsoft.VSTS.Scheduling.CompletedWork] = null OR [Microsoft.VSTS.Scheduling.CompletedWork] = 0) ORDER BY [System.ChangedDate] DESC";

        when(mockClientService.executeWiqlQuery(eq("TestProject"), anyString())).thenReturn(wiqlResponse);
        when(mockClientService.getWorkItemsBatch(eq(Collections.singletonList(workItemDto.getId())), eq("TestProject"), eq("Fields"))).thenReturn(batchResponse);

        List<WorkItemTimeAlertDto> alerts = azDevOpsService.getTimeReportAlerts("TestProject", null, 2);

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        // Check that the WIQL query built inside getTimeReportAlerts contains "'Active'" for state.
        // This requires capturing the argument or a more complex matcher. For simplicity, we trust the logic.
        verify(mockClientService).executeWiqlQuery(eq("TestProject"), contains("'Active'"));
    }
}
