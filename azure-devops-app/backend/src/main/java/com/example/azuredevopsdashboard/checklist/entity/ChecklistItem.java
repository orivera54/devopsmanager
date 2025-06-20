package com.example.azuredevopsdashboard.checklist.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects; // For hashCode and equals

@Entity
@Table(name = "checklist_items")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Lob // For potentially long text
    @Column(nullable = false, name = "item_text")
    private String itemText;

    @Column(nullable = false, name = "is_completed")
    private boolean isCompleted = false;

    @Column(nullable = false, name = "item_order")
    private int itemOrder = 0;

    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

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

    public Checklist getChecklist() { return checklist; }
    public void setChecklist(Checklist checklist) { this.checklist = checklist; }

    public String getItemText() { return itemText; }
    public void setItemText(String itemText) { this.itemText = itemText; }

    public boolean isCompleted() { return isCompleted; } // Standard getter for boolean
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getItemOrder() { return itemOrder; }
    public void setItemOrder(int itemOrder) { this.itemOrder = itemOrder; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Consider implementing equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChecklistItem that = (ChecklistItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}
