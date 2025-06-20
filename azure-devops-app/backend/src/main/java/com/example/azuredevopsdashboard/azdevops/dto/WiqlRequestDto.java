package com.example.azuredevopsdashboard.azdevops.dto;

public class WiqlRequestDto {
    private String query;

    public WiqlRequestDto(String query) {
        this.query = query;
    }

    // Getter and Setter
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
