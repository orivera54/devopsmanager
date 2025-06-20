package com.example.azuredevopsdashboard.checklist.dto;

public class ChecklistItemDto {
    private Long id;
    private String itemText;
    private boolean isCompleted;
    private int itemOrder;

    // Constructors
    public ChecklistItemDto() {}

    public ChecklistItemDto(Long id, String itemText, boolean isCompleted, int itemOrder) {
        this.id = id;
        this.itemText = itemText;
        this.isCompleted = isCompleted;
        this.itemOrder = itemOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemText() { return itemText; }
    public void setItemText(String itemText) { this.itemText = itemText; }

    public boolean isCompleted() { return isCompleted; } // Standard getter for boolean
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getItemOrder() { return itemOrder; }
    public void setItemOrder(int itemOrder) { this.itemOrder = itemOrder; }
}
