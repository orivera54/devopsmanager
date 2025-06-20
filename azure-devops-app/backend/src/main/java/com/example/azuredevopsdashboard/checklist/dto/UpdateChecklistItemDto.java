package com.example.azuredevopsdashboard.checklist.dto;

// import jakarta.validation.constraints.Size;

public class UpdateChecklistItemDto {

    // @Size(max = 10000, message = "Item text is too long") // Example validation for text length
    private String itemText; // Optional: only provide if text needs to change

    private Boolean isCompleted; // Optional: Boolean wrapper allows distinguishing between false and not provided

    private Integer itemOrder; // Optional: Integer wrapper allows distinguishing between 0 and not provided

    // Default constructor for Jackson
    public UpdateChecklistItemDto() {}

    // Getters and Setters
    public String getItemText() { return itemText; }
    public void setItemText(String itemText) { this.itemText = itemText; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean completed) { isCompleted = completed; }

    public Integer getItemOrder() { return itemOrder; }
    public void setItemOrder(Integer itemOrder) { this.itemOrder = itemOrder; }
}
