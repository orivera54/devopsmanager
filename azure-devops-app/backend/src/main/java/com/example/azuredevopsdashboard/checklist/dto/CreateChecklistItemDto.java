package com.example.azuredevopsdashboard.checklist.dto;

// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull; // Not typically needed for boolean/int primitives

public class CreateChecklistItemDto {

    // @NotBlank(message = "Item text cannot be blank")
    private String itemText;

    private boolean isCompleted = false; // Default to false

    private int itemOrder = 0; // Default order, service might override or calculate

    // Default constructor for Jackson
    public CreateChecklistItemDto() {}

    // Getters and Setters
    public String getItemText() { return itemText; }
    public void setItemText(String itemText) { this.itemText = itemText; }

    public boolean isCompleted() { return isCompleted; } // Standard getter for boolean
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getItemOrder() { return itemOrder; }
    public void setItemOrder(int itemOrder) { this.itemOrder = itemOrder; }
}
