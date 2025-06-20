package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsWorkItemCommentDto {

    // In the context of GET /_apis/wit/workItems/{workItemId}/comments,
    // the 'id' field in the response objects refers to the comment's ID.
    @JsonProperty("id")
    private int commentId;

    private int revision;
    private String text; // This will be markdown. Use $expand=renderedText to get HTML if needed.
    private String renderedText; // Populated if $expand=renderedText is used
    private AzDevOpsUserReferenceDto createdBy;
    private Instant createdDate;
    private AzDevOpsUserReferenceDto modifiedBy;
    private Instant modifiedDate;
    private String url; // URL to the comment itself within the API

    // Getters and Setters
    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getRevision() { return revision; }
    public void setRevision(int revision) { this.revision = revision; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getRenderedText() { return renderedText; }
    public void setRenderedText(String renderedText) { this.renderedText = renderedText; }

    public AzDevOpsUserReferenceDto getCreatedBy() { return createdBy; }
    public void setCreatedBy(AzDevOpsUserReferenceDto createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public AzDevOpsUserReferenceDto getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(AzDevOpsUserReferenceDto modifiedBy) { this.modifiedBy = modifiedBy; }

    public Instant getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(Instant modifiedDate) { this.modifiedDate = modifiedDate; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
