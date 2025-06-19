package com.example.azuredevopsdashboard.payload;

// Add necessary imports for validation if desired, e.g., jakarta.validation.constraints.NotBlank

public class LoginRequest {
    // @jakarta.validation.constraints.NotBlank // Uncomment if validation dependency is added
    private String username;

    // @jakarta.validation.constraints.NotBlank // Uncomment if validation dependency is added
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
