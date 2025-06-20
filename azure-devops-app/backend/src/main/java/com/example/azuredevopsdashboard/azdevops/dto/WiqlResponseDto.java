package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.time.Instant; // For queryResultType and asOf

@JsonIgnoreProperties(ignoreUnknown = true)
public class WiqlResponseDto {
    private String queryType; // e.g., "flat"
    private String queryResultType; // e.g., "workItem"
    private Instant asOf;
    // private List<Object> columns; // If columns are defined explicitly in query and needed
    // private List<Object> sortColumns; // If sort columns are defined explicitly
    private List<WiqlWorkItemReferenceDto> workItems;

    // For hierarchical queries (tree), the structure is different and would involve WorkItemLinkDto
    // private List<WorkItemLinkDto> workItemRelations;

    // Getters and Setters
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getQueryResultType() { return queryResultType; }
    public void setQueryResultType(String queryResultType) { this.queryResultType = queryResultType; }

    public Instant getAsOf() { return asOf; }
    public void setAsOf(Instant asOf) { this.asOf = asOf; }

    public List<WiqlWorkItemReferenceDto> getWorkItems() { return workItems; }
    public void setWorkItems(List<WiqlWorkItemReferenceDto> workItems) { this.workItems = workItems; }
}
