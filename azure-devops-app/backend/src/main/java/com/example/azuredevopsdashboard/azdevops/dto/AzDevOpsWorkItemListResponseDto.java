package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsWorkItemListResponseDto {
    private int count;
    private List<AzDevOpsWorkItemDto> value;

    // Getters and Setters
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<AzDevOpsWorkItemDto> getValue() { return value; }
    public void setValue(List<AzDevOpsWorkItemDto> value) { this.value = value; }
}
