package com.example.azuredevopsdashboard.checklist.repository;

import com.example.azuredevopsdashboard.checklist.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    /**
     * Finds all checklist items for a given checklist ID, ordered by their itemOrder.
     * This is useful if items are not always fetched eagerly with the Checklist entity,
     * or if @OrderBy on the collection in Checklist entity is not used/sufficient.
     *
     * @param checklistId The ID of the checklist.
     * @return A list of checklist items sorted by itemOrder.
     */
    List<ChecklistItem> findByChecklistIdOrderByItemOrderAsc(Long checklistId);

    // Additional query methods can be added here, for example:
    // List<ChecklistItem> findByChecklistIdAndIsCompleted(Long checklistId, boolean isCompleted);
}
