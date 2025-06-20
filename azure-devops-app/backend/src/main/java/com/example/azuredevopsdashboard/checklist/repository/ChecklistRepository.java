package com.example.azuredevopsdashboard.checklist.repository;

import com.example.azuredevopsdashboard.checklist.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // For custom queries
import org.springframework.data.repository.query.Param; // For named parameters in custom queries
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    /**
     * Finds all checklists associated with a specific Azure DevOps work item.
     *
     * @param organization The Azure DevOps organization name.
     * @param projectName  The Azure DevOps project name.
     * @param workItemId   The Azure DevOps work item ID.
     * @return A list of checklists.
     */
    List<Checklist> findByAzureDevopsOrganizationAndAzureDevopsProjectNameAndAzureDevopsWorkItemId(
        String organization,
        String projectName,
        Integer workItemId
    );

    /**
     * Finds a checklist by its ID and eagerly fetches its items.
     * This can help avoid N+1 problems if items are frequently accessed with the checklist.
     *
     * @param id The ID of the checklist.
     * @return An Optional containing the checklist with its items, or empty if not found.
     */
    @Query("SELECT c FROM Checklist c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Optional<Checklist> findByIdWithItems(@Param("id") Long id);

    // Additional query methods can be added here as needed, for example:
    // List<Checklist> findByCreatedByUser_Id(Integer userId);
    // List<Checklist> findByNameContainingIgnoreCase(String name);
}
