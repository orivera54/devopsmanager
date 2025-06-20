package com.example.azuredevopsdashboard.azdevops.dto;

// Consider adding validation annotations if a validation starter is present
// import jakarta.validation.constraints.NotBlank;

public class WorkItemQueryRequestDto {

    // @NotBlank(message = "WIQL query cannot be blank") // Example validation
    private String wiql;

    // Default constructor for JSON deserialization
    public WorkItemQueryRequestDto() {
    }

    public WorkItemQueryRequestDto(String wiql) {
        this.wiql = wiql;
    }

    // Getters and Setters
    public String getWiql() { return wiql; }
    public void setWiql(String wiql) { this.wiql = wiql; }
}
