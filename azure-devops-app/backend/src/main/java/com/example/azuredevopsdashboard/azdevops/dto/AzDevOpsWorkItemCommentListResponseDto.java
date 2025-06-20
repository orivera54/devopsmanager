package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty; // For specific field name mapping
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsWorkItemCommentListResponseDto {

    // The API GET .../workItems/{workItemId}/comments returns an object with these fields.
    // See: https://learn.microsoft.com/en-us/rest/api/azure/devops/wit/comments/get-comments?view=azure-devops-rest-7.1&tabs=HTTP

    private int count; // Number of comments in the current page/batch
    private int totalCount; // Total number of comments available

    @JsonProperty("comments") // The actual list of comments is under the "comments" field in the JSON response
    private List<AzDevOpsWorkItemCommentDto> commentList;

    // Getters and Setters
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public List<AzDevOpsWorkItemCommentDto> getCommentList() { return commentList; }
    public void setCommentList(List<AzDevOpsWorkItemCommentDto> commentList) { this.commentList = commentList; }
}
