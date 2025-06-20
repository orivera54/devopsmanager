package com.example.azuredevopsdashboard.checklist.entity;

import com.example.azuredevopsdashboard.user.User; // Import the User entity
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // For hashCode and equals

@Entity
@Table(name = "checklists")
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false, name = "azure_devops_organization")
    private String azureDevopsOrganization;

    @Column(nullable = false, name = "azure_devops_project_name")
    private String azureDevopsProjectName;

    @Column(nullable = false, name = "azure_devops_work_item_id")
    private Integer azureDevopsWorkItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id") // This column is in 'checklists' table
    private User createdByUser;

    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("itemOrder ASC")
    private List<ChecklistItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAzureDevopsOrganization() { return azureDevopsOrganization; }
    public void setAzureDevopsOrganization(String azureDevopsOrganization) { this.azureDevopsOrganization = azureDevopsOrganization; }

    public String getAzureDevopsProjectName() { return azureDevopsProjectName; }
    public void setAzureDevopsProjectName(String azureDevopsProjectName) { this.azureDevopsProjectName = azureDevopsProjectName; }

    public Integer getAzureDevopsWorkItemId() { return azureDevopsWorkItemId; }
    public void setAzureDevopsWorkItemId(Integer azureDevopsWorkItemId) { this.azureDevopsWorkItemId = azureDevopsWorkItemId; }

    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User createdByUser) { this.createdByUser = createdByUser; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ChecklistItem> getItems() { return items; }
    public void setItems(List<ChecklistItem> items) { this.items = items; }

    // Helper methods for managing the bidirectional association
    public void addItem(ChecklistItem item) {
        if (item != null) {
            items.add(item);
            item.setChecklist(this);
        }
    }

    public void removeItem(ChecklistItem item) {
        if (item != null) {
            items.remove(item);
            item.setChecklist(null);
        }
    }

    // Consider implementing equals and hashCode if these entities are used in Sets or as Map keys.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Checklist checklist = (Checklist) o;
        // Use a field that is persistent and unique, like ID, after it's generated.
        // For new (unpersisted) entities, this might rely on business key equality or reference equality.
        return id != null && id.equals(checklist.id);
    }

    @Override
    public int hashCode() {
        // Use a prime number and the ID if available, otherwise rely on Object's hashCode.
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}
