package com.example.azuredevopsdashboard.payload;

// Add necessary imports for validation (e.g., @NotBlank, @Email, @Size from jakarta.validation.constraints)

public class SignUpRequest {
    // @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 3, max = 20)
    private String username;

    // @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email @jakarta.validation.constraints.Size(max = 50)
    private String email;

    // @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 6, max = 100)
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
