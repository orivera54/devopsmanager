package com.example.azuredevopsdashboard.checklist.controller;

import com.example.azuredevopsdashboard.checklist.dto.*;
import com.example.azuredevopsdashboard.checklist.service.ChecklistService;
import com.example.azuredevopsdashboard.user.User;
import com.example.azuredevopsdashboard.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // More specific
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils; // For StringUtils.hasText

import jakarta.persistence.EntityNotFoundException;
// import jakarta.validation.Valid; // Uncomment if validation is added to DTOs

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    private static final Logger logger = LoggerFactory.getLogger(ChecklistController.class);

    private final ChecklistService checklistService;
    private final UserRepository userRepository;

    @Autowired
    public ChecklistController(ChecklistService checklistService, UserRepository userRepository) {
        this.checklistService = checklistService;
        this.userRepository = userRepository;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            // For APIs that require authentication, this should ideally be caught by Spring Security earlier.
            // Throwing an exception here or relying on SecurityConfig to deny access.
            // If execution reaches here and user is anonymous, it implies endpoint might be misconfigured (e.g. permitAll())
            // or this method is called where optional authentication is a factor.
            logger.warn("Attempt to access resource without authentication or as anonymousUser.");
            throw new UsernameNotFoundException("User not authenticated. Cannot perform this action.");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("Authenticated user '{}' not found in database.", username);
                return new UsernameNotFoundException("Authenticated user not found in database: " + username);
            });
    }

    @PostMapping
    public ResponseEntity<ChecklistDto> createChecklist(@RequestBody /* @Valid */ CreateChecklistRequestDto request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            ChecklistDto createdChecklist = checklistService.createChecklist(request, currentUser);
            logger.info("Checklist created with ID: {} by user: {}", createdChecklist.getId(), currentUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChecklist);
        } catch (UsernameNotFoundException e) {
            logger.warn("Unauthorized attempt to create checklist: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Error creating checklist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/for-workitem")
    public ResponseEntity<List<ChecklistDto>> getChecklistsForWorkItem(
            @RequestParam String organization,
            @RequestParam String project,
            @RequestParam Integer workItemId) {
        // Basic validation for required parameters
        if (!StringUtils.hasText(organization) || !StringUtils.hasText(project) || workItemId == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<ChecklistDto> checklists = checklistService.getChecklistsForWorkItem(organization, project, workItemId);
            return ResponseEntity.ok(checklists);
        } catch (Exception e) {
            logger.error("Error fetching checklists for work item {}/{}/{}: {}", organization, project, workItemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{checklistId}")
    public ResponseEntity<ChecklistDto> getChecklistById(@PathVariable Long checklistId) {
        try {
            Optional<ChecklistDto> checklistDto = checklistService.getChecklistById(checklistId);
            return checklistDto.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching checklist by ID {}: {}", checklistId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{checklistId}/info")
    public ResponseEntity<ChecklistDto> updateChecklistInfo(
            @PathVariable Long checklistId,
            @RequestBody /* @Valid */ CreateChecklistRequestDto request) {
        try {
            // Ensure current user has rights to update this checklist (e.g., is owner or admin)
            // This logic would typically be in the service layer or handled via Spring Security method-level security.
            // User currentUser = getCurrentAuthenticatedUser(); // Example if needed for authorization
            ChecklistDto updatedChecklist = checklistService.updateChecklistInfo(checklistId, request);
            return ResponseEntity.ok(updatedChecklist);
        } catch (EntityNotFoundException e) {
            logger.warn("Update failed. Checklist not found: {}", checklistId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating checklist info for ID {}: {}", checklistId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{checklistId}")
    public ResponseEntity<Void> deleteChecklist(@PathVariable Long checklistId) {
        try {
            // Add authorization check here if needed: e.g., only creator or admin can delete.
            // User currentUser = getCurrentAuthenticatedUser();
            checklistService.deleteChecklist(checklistId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Delete failed. Checklist not found: {}", checklistId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting checklist ID {}: {}", checklistId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Checklist Items Endpoints ---

    @PostMapping("/{checklistId}/items")
    public ResponseEntity<?> addChecklistItem( // Return type can be more specific like ResponseEntity<ChecklistItemDto>
            @PathVariable Long checklistId,
            @RequestBody /* @Valid */ CreateChecklistItemDto itemRequest) {
        try {
            // User currentUser = getCurrentAuthenticatedUser(); // Authorization check if needed
            ChecklistItemDto createdItem = checklistService.addChecklistItem(checklistId, itemRequest);
            if (createdItem.getId() == null) { // Check if item ID was populated (see service comments)
                 logger.warn("Checklist item was added to checklist {} but ID retrieval failed. Client might need to refetch checklist.", checklistId);
                 // Returning 200 OK with the (potentially ID-less) item, or 202 Accepted if processing is complex.
                 return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdItem);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (EntityNotFoundException e) {
            logger.warn("Cannot add item. Checklist not found: {}", checklistId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding item to checklist ID {}: {}", checklistId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ChecklistItemDto> updateChecklistItem(
            @PathVariable Long itemId,
            @RequestBody /* @Valid */ UpdateChecklistItemDto itemRequest) {
        try {
            // User currentUser = getCurrentAuthenticatedUser(); // Authorization check if needed
            ChecklistItemDto updatedItem = checklistService.updateChecklistItem(itemId, itemRequest);
            return ResponseEntity.ok(updatedItem);
        } catch (EntityNotFoundException e) {
            logger.warn("Cannot update. Checklist item not found: {}", itemId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating checklist item ID {}: {}", itemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteChecklistItem(@PathVariable Long itemId) {
        try {
            // User currentUser = getCurrentAuthenticatedUser(); // Authorization check if needed
            checklistService.deleteChecklistItem(itemId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Cannot delete. Checklist item not found: {}", itemId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting checklist item ID {}: {}", itemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
