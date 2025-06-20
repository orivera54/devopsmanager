package com.example.azuredevopsdashboard.checklist.service;

import com.example.azuredevopsdashboard.checklist.dto.*;
import com.example.azuredevopsdashboard.checklist.entity.Checklist;
import com.example.azuredevopsdashboard.checklist.entity.ChecklistItem;
import com.example.azuredevopsdashboard.checklist.repository.ChecklistItemRepository;
import com.example.azuredevopsdashboard.checklist.repository.ChecklistRepository;
import com.example.azuredevopsdashboard.user.User;
// UserRepository might be needed if we were to fetch User entities by ID passed in a request,
// but for setting createdByUser, we expect a managed User entity.
// import com.example.azuredevopsdashboard.user.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils; // For checking empty collections

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChecklistService {

    private static final Logger logger = LoggerFactory.getLogger(ChecklistService.class);

    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository checklistItemRepository;
    // private final UserRepository userRepository; // Not used in this version

    @Autowired
    public ChecklistService(ChecklistRepository checklistRepository,
                            ChecklistItemRepository checklistItemRepository
                            /*, UserRepository userRepository */) {
        this.checklistRepository = checklistRepository;
        this.checklistItemRepository = checklistItemRepository;
        // this.userRepository = userRepository;
    }

    // --- Mapper Static Inner Class ---
    // (Could be a separate @Component with MapStruct for more complex mapping)
    private static class ChecklistMapper {
        static ChecklistItemDto toChecklistItemDto(ChecklistItem item) {
            if (item == null) return null;
            return new ChecklistItemDto(item.getId(), item.getItemText(), item.isCompleted(), item.getItemOrder());
        }

        static List<ChecklistItemDto> toChecklistItemDtoList(List<ChecklistItem> items) {
            if (CollectionUtils.isEmpty(items)) return Collections.emptyList();
            return items.stream().map(ChecklistMapper::toChecklistItemDto).collect(Collectors.toList());
        }

        static ChecklistDto toChecklistDto(Checklist checklist) {
            if (checklist == null) return null;
            ChecklistDto dto = new ChecklistDto();
            dto.setId(checklist.getId());
            dto.setName(checklist.getName());
            dto.setDescription(checklist.getDescription());
            dto.setAzureDevopsOrganization(checklist.getAzureDevopsOrganization());
            dto.setAzureDevopsProjectName(checklist.getAzureDevopsProjectName());
            dto.setAzureDevopsWorkItemId(checklist.getAzureDevopsWorkItemId());
            if (checklist.getCreatedByUser() != null) {
                dto.setCreatedByUserId(checklist.getCreatedByUser().getId()); // Assumes User.id is Integer
                dto.setCreatedByUsername(checklist.getCreatedByUser().getUsername());
            }
            dto.setCreatedAt(checklist.getCreatedAt());
            dto.setUpdatedAt(checklist.getUpdatedAt());
            // Eagerly fetch items for the DTO if they are LAZY and accessed outside a transaction
            // Or ensure items are fetched if needed by the caller context (e.g. using findByIdWithItems)
            dto.setItems(toChecklistItemDtoList(checklist.getItems()));
            return dto;
        }

        static List<ChecklistDto> toChecklistDtoList(List<Checklist> checklists) {
            if (CollectionUtils.isEmpty(checklists)) return Collections.emptyList();
            return checklists.stream().map(ChecklistMapper::toChecklistDto).collect(Collectors.toList());
        }
    }
    // --- End of Mapper ---

    @Transactional
    public ChecklistDto createChecklist(CreateChecklistRequestDto request, User currentUser) {
        Checklist checklist = new Checklist();
        checklist.setName(request.getName());
        checklist.setDescription(request.getDescription());
        checklist.setAzureDevopsOrganization(request.getAzureDevopsOrganization());
        checklist.setAzureDevopsProjectName(request.getAzureDevopsProjectName());
        checklist.setAzureDevopsWorkItemId(request.getAzureDevopsWorkItemId());

        if (currentUser == null) {
            logger.warn("Attempting to create checklist without a current user. 'createdByUser' will be null.");
            // Depending on business rules, you might throw an exception here or allow anonymous creation.
        }
        checklist.setCreatedByUser(currentUser);

        if (!CollectionUtils.isEmpty(request.getItems())) {
            for (CreateChecklistItemDto itemDto : request.getItems()) {
                ChecklistItem item = new ChecklistItem();
                item.setItemText(itemDto.getItemText());
                item.setCompleted(itemDto.isCompleted());
                item.setItemOrder(itemDto.getItemOrder());
                checklist.addItem(item); // This also sets item.setChecklist(this)
            }
        }
        Checklist savedChecklist = checklistRepository.save(checklist);
        logger.info("Created checklist ID: {} with {} items for ADO WI ID: {}", savedChecklist.getId(), savedChecklist.getItems().size(), savedChecklist.getAzureDevopsWorkItemId());
        return ChecklistMapper.toChecklistDto(savedChecklist);
    }

    @Transactional(readOnly = true)
    public List<ChecklistDto> getChecklistsForWorkItem(String org, String project, Integer workItemId) {
        logger.debug("Fetching checklists for ADO WI: {}/{}/{}", org, project, workItemId);
        List<Checklist> checklists = checklistRepository.findByAzureDevopsOrganizationAndAzureDevopsProjectNameAndAzureDevopsWorkItemId(org, project, workItemId);
        return ChecklistMapper.toChecklistDtoList(checklists);
    }

    @Transactional(readOnly = true)
    public Optional<ChecklistDto> getChecklistById(Long checklistId) {
        logger.debug("Fetching checklist by ID: {}", checklistId);
        // Using findByIdWithItems to ensure items are fetched if the Checklist entity has LAZY fetch for items.
        return checklistRepository.findByIdWithItems(checklistId).map(ChecklistMapper::toChecklistDto);
    }

    @Transactional
    public ChecklistDto updateChecklistInfo(Long checklistId, CreateChecklistRequestDto request) {
        Checklist checklist = checklistRepository.findById(checklistId)
            .orElseThrow(() -> new EntityNotFoundException("Checklist not found with id: " + checklistId));

        checklist.setName(request.getName());
        checklist.setDescription(request.getDescription());
        // Not allowing change of ADO linkage or createdByUser via this method.

        Checklist updatedChecklist = checklistRepository.save(checklist);
        logger.info("Updated info for checklist ID: {}", updatedChecklist.getId());
        return ChecklistMapper.toChecklistDto(updatedChecklist);
    }

    @Transactional
    public void deleteChecklist(Long checklistId) {
        logger.info("Attempting to delete checklist ID: {}", checklistId);
        if (!checklistRepository.existsById(checklistId)) {
            logger.warn("Delete failed. Checklist not found with id: {}", checklistId);
            throw new EntityNotFoundException("Checklist not found with id: " + checklistId);
        }
        checklistRepository.deleteById(checklistId);
        logger.info("Successfully deleted checklist ID: {}", checklistId);
    }

    @Transactional
    public ChecklistItemDto addChecklistItem(Long checklistId, CreateChecklistItemDto itemRequest) {
        Checklist checklist = checklistRepository.findByIdWithItems(checklistId) // Fetch with items to avoid issues if items collection is lazy
            .orElseThrow(() -> new EntityNotFoundException("Checklist not found with id: " + checklistId + " to add item."));

        ChecklistItem item = new ChecklistItem();
        item.setItemText(itemRequest.getItemText());
        item.setCompleted(itemRequest.isCompleted());

        // Determine itemOrder: if not provided, append to the end.
        if (itemRequest.getItemOrder() == 0 && !checklist.getItems().isEmpty()) {
            item.setItemOrder(checklist.getItems().stream().mapToInt(ChecklistItem::getItemOrder).max().orElse(0) + 1);
        } else {
            item.setItemOrder(itemRequest.getItemOrder());
        }

        checklist.addItem(item); // This sets item.setChecklist(checklist)

        // Checklist is saved, and due to CascadeType.ALL, the new item is also saved.
        Checklist savedChecklist = checklistRepository.save(checklist);

        // Retrieve the saved item from the list (it should have an ID now)
        // This assumes the item added is the last one or identifiable if order isn't guaranteed post-save without re-fetch.
        // A more robust way might be to find the item by text and order if ID is null after save.
        ChecklistItem savedItem = savedChecklist.getItems().stream()
            .filter(i -> i.getItemText().equals(itemRequest.getItemText()) && i.getItemOrder() == item.getItemOrder() && i.getId() != null)
            .findFirst()
            .orElse(item); // Fallback to original item if not found (ID might still be null)

        if(savedItem.getId() == null) {
            logger.warn("Saved checklist item ID not populated immediately for checklist {}. The item might not have been persisted as expected or ID retrieval failed.", checklistId);
            // This might happen if the transaction isn't flushed or if the 'item' instance isn't updated by JPA post-cascade.
            // For now, we return the DTO, but it might lack an ID.
        }
        logger.info("Added item to checklist ID: {}. New item ID (if available): {}", checklistId, savedItem.getId());
        return ChecklistMapper.toChecklistItemDto(savedItem);
    }

    @Transactional
    public ChecklistItemDto updateChecklistItem(Long itemId, UpdateChecklistItemDto itemRequest) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException("ChecklistItem not found with id: " + itemId));

        boolean changed = false;
        if (itemRequest.getItemText() != null && !itemRequest.getItemText().equals(item.getItemText())) {
            item.setItemText(itemRequest.getItemText());
            changed = true;
        }
        if (itemRequest.getIsCompleted() != null && itemRequest.getIsCompleted() != item.isCompleted()) {
            item.setCompleted(itemRequest.getIsCompleted());
            changed = true;
        }
        if (itemRequest.getItemOrder() != null && itemRequest.getItemOrder() != item.getItemOrder()) {
            item.setItemOrder(itemRequest.getItemOrder());
            changed = true;
            // Note: Changing itemOrder here does not automatically re-order other items.
            // Complex reordering logic would be needed if sibling items should shift.
        }

        if (changed) {
            ChecklistItem updatedItem = checklistItemRepository.save(item);
            logger.info("Updated checklist item ID: {}", updatedItem.getId());
            return ChecklistMapper.toChecklistItemDto(updatedItem);
        }
        logger.debug("No changes detected for checklist item ID: {}", itemId);
        return ChecklistMapper.toChecklistItemDto(item);
    }

    @Transactional
    public void deleteChecklistItem(Long itemId) {
        logger.info("Attempting to delete checklist item ID: {}", itemId);
        ChecklistItem item = checklistItemRepository.findById(itemId)
            .orElseThrow(() -> {
                logger.warn("Delete failed. ChecklistItem not found with id: {}", itemId);
                return new EntityNotFoundException("ChecklistItem not found with id: " + itemId);
            });

        // Option 1: If orphanRemoval=true is reliable and item is removed from collection
        // Checklist checklist = item.getChecklist();
        // if (checklist != null) {
        //     checklist.removeItem(item); // This should trigger orphan removal
        //     checklistRepository.save(checklist);
        // } else {
        //     checklistItemRepository.delete(item); // Fallback if no parent link (should not happen with optional=false)
        // }

        // Option 2: Direct delete (simpler, usually works fine)
        checklistItemRepository.delete(item);
        logger.info("Successfully deleted checklist item ID: {}", itemId);
    }
}
