package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsProjectListResponseDto {
    private int count;
    private List<AzDevOpsProjectDto> value;

    // Getters and Setters
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<AzDevOpsProjectDto> getValue() { return value; }
    public void setValue(List<AzDevOpsProjectDto> value) { this.value = value; }
}
