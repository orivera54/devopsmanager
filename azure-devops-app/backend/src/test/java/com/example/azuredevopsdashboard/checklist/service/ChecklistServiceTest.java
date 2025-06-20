package com.example.azuredevopsdashboard.checklist.service;

import com.example.azuredevopsdashboard.checklist.dto.*;
// Correcting the import for ChecklistItem if it's a static nested class or separate.
// If ChecklistItem is a top-level class in checklist.entity, this is correct:
import com.example.azuredevopsdashboard.checklist.entity.ChecklistItem;
import com.example.azuredevopsdashboard.checklist.entity.Checklist;
import com.example.azuredevopsdashboard.checklist.repository.ChecklistItemRepository;
import com.example.azuredevopsdashboard.checklist.repository.ChecklistRepository;
import com.example.azuredevopsdashboard.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant; // For setting createdAt/updatedAt if needed in tests
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock
    private ChecklistRepository mockChecklistRepository;
    @Mock
    private ChecklistItemRepository mockChecklistItemRepository;
    // @Mock private UserRepository mockUserRepository; // Not injected in current service version

    @InjectMocks
    private ChecklistService checklistService;

    private User testUser;
    private CreateChecklistRequestDto createRequest;
    private Checklist checklistEntity;
    private ChecklistItem checklistItemEntity;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1); // Ensure ID type matches User entity (Integer)
        testUser.setUsername("testuser");

        createRequest = new CreateChecklistRequestDto();
        createRequest.setName("Test Checklist");
        createRequest.setDescription("A test checklist.");
        createRequest.setAzureDevopsOrganization("TestOrg");
        createRequest.setAzureDevopsProjectName("TestProject");
        createRequest.setAzureDevopsWorkItemId(100);

        CreateChecklistItemDto itemDto = new CreateChecklistItemDto();
        itemDto.setItemText("First item");
        itemDto.setItemOrder(0);
        createRequest.setItems(Collections.singletonList(itemDto));

        checklistEntity = new Checklist();
        checklistEntity.setId(1L);
        checklistEntity.setName(createRequest.getName());
        checklistEntity.setCreatedByUser(testUser);
        checklistEntity.setAzureDevopsOrganization(createRequest.getAzureDevopsOrganization());
        checklistEntity.setAzureDevopsProjectName(createRequest.getAzureDevopsProjectName());
        checklistEntity.setAzureDevopsWorkItemId(createRequest.getAzureDevopsWorkItemId());
        checklistEntity.setCreatedAt(Instant.now()); // Set this for DTO mapping
        checklistEntity.setUpdatedAt(Instant.now()); // Set this for DTO mapping

        checklistItemEntity = new ChecklistItem();
        checklistItemEntity.setId(10L);
        checklistItemEntity.setItemText("First item text from entity");
        checklistItemEntity.setItemOrder(0);
        checklistItemEntity.setCompleted(false);
        // checklistEntity.addItem(checklistItemEntity); // This would be done by service or if loading existing
    }

    @Test
    void createChecklist_shouldSaveAndReturnDto() {
        // Arrange
        when(mockChecklistRepository.save(any(Checklist.class))).thenAnswer(invocation -> {
            Checklist saved = invocation.getArgument(0);
            if (saved.getId() == null) saved.setId(1L + (long)(Math.random()*1000)); // Simulate ID generation

            // Simulate ID generation for items if they are new
            long itemIdCounter = 10L;
            if (saved.getItems() != null) {
                for(ChecklistItem item : saved.getItems()){
                    if(item.getId() == null) item.setId(itemIdCounter++);
                    // Ensure bidirectional link for mapper if it relies on it
                    item.setChecklist(saved);
                }
            }
            return saved;
        });

        // Act
        ChecklistDto resultDto = checklistService.createChecklist(createRequest, testUser);

        // Assert
        assertNotNull(resultDto);
        assertEquals(createRequest.getName(), resultDto.getName());
        assertEquals(testUser.getId(), resultDto.getCreatedByUserId());
        assertEquals(testUser.getUsername(), resultDto.getCreatedByUsername());
        assertFalse(resultDto.getItems().isEmpty());
        assertEquals("First item", resultDto.getItems().get(0).getItemText());
        assertNotNull(resultDto.getItems().get(0).getId()); // Check if ID was set by mock

        ArgumentCaptor<Checklist> checklistCaptor = ArgumentCaptor.forClass(Checklist.class);
        verify(mockChecklistRepository).save(checklistCaptor.capture());

        Checklist capturedChecklist = checklistCaptor.getValue();
        assertEquals(createRequest.getName(), capturedChecklist.getName());
        assertEquals(testUser, capturedChecklist.getCreatedByUser());
        assertEquals(1, capturedChecklist.getItems().size());
        assertEquals("First item", capturedChecklist.getItems().get(0).getItemText());
    }

    @Test
    void getChecklistsForWorkItem_shouldReturnListOfDtos() {
        // Arrange
        // Add item to the entity for the mapper to pick up
        checklistEntity.setItems(Collections.singletonList(checklistItemEntity));
        checklistItemEntity.setChecklist(checklistEntity); // Ensure bidirectional link if mapper needs it

        when(mockChecklistRepository.findByAzureDevopsOrganizationAndAzureDevopsProjectNameAndAzureDevopsWorkItemId(
            "TestOrg", "TestProject", 100))
            .thenReturn(Collections.singletonList(checklistEntity));

        // Act
        List<ChecklistDto> result = checklistService.getChecklistsForWorkItem("TestOrg", "TestProject", 100);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ChecklistDto dto = result.get(0);
        assertEquals(checklistEntity.getName(), dto.getName());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        assertEquals(checklistItemEntity.getItemText(), dto.getItems().get(0).getItemText());

        verify(mockChecklistRepository).findByAzureDevopsOrganizationAndAzureDevopsProjectNameAndAzureDevopsWorkItemId("TestOrg", "TestProject", 100);
    }

    @Test
    void updateChecklistItem_whenItemExists_shouldUpdateAndReturnDto() {
        // Arrange
        Long itemId = 1L;
        UpdateChecklistItemDto updateDto = new UpdateChecklistItemDto();
        updateDto.setItemText("Updated Text");
        updateDto.setIsCompleted(true);
        updateDto.setItemOrder(1);

        ChecklistItem existingItem = new ChecklistItem(); // Use top-level ChecklistItem
        existingItem.setId(itemId);
        existingItem.setItemText("Old Text");
        existingItem.setCompleted(false);
        existingItem.setItemOrder(0);

        when(mockChecklistItemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(mockChecklistItemRepository.save(any(ChecklistItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ChecklistItemDto result = checklistService.updateChecklistItem(itemId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Text", result.getItemText());
        assertTrue(result.isCompleted());
        assertEquals(1, result.getItemOrder());
        verify(mockChecklistItemRepository).findById(itemId);
        verify(mockChecklistItemRepository).save(existingItem);
    }

    @Test
    void updateChecklistItem_whenItemNotFound_shouldThrowException() {
        // Arrange
        Long itemId = 1L;
        UpdateChecklistItemDto updateDto = new UpdateChecklistItemDto();
        when(mockChecklistItemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            checklistService.updateChecklistItem(itemId, updateDto);
        });
        verify(mockChecklistItemRepository).findById(itemId);
        verify(mockChecklistItemRepository, never()).save(any());
    }

    @Test
    void deleteChecklist_whenExists_shouldCallDelete() {
        Long checklistId = 1L;
        when(mockChecklistRepository.existsById(checklistId)).thenReturn(true);
        doNothing().when(mockChecklistRepository).deleteById(checklistId);

        checklistService.deleteChecklist(checklistId);

        verify(mockChecklistRepository).existsById(checklistId);
        verify(mockChecklistRepository).deleteById(checklistId);
    }

    @Test
    void deleteChecklist_whenNotExists_shouldThrowException() {
        Long checklistId = 1L;
        when(mockChecklistRepository.existsById(checklistId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            checklistService.deleteChecklist(checklistId);
        });
        verify(mockChecklistRepository).existsById(checklistId);
        verify(mockChecklistRepository, never()).deleteById(anyLong());
    }
}
