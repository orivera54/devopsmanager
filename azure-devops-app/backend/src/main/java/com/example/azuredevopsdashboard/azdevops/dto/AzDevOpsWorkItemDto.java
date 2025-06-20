package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzDevOpsWorkItemDto {
    private int id;
    private int rev; // Revision
    private AzDevOpsWorkItemFieldsDto fields;
    private String url; // URL to the API of the work item

    @JsonProperty("_links")
    private Map<String, HrefDto> links; // To capture _links, e.g., html link

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRev() { return rev; }
    public void setRev(int rev) { this.rev = rev; }

    public AzDevOpsWorkItemFieldsDto getFields() { return fields; }
    public void setFields(AzDevOpsWorkItemFieldsDto fields) { this.fields = fields; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, HrefDto> getLinks() { return links; }
    public void setLinks(Map<String, HrefDto> links) { this.links = links; }

    // Helper method to get the HTML link if it exists
    public String getHtmlLink() {
        if (links != null && links.containsKey("html")) {
            HrefDto htmlLink = links.get("html");
            if (htmlLink != null) {
                return htmlLink.getHref();
            }
        }
        return null;
    }

    // Helper method to get the Parent link if it exists
    public String getParentLink() {
         if (links != null && links.containsKey("parent")) {
            HrefDto parentLink = links.get("parent");
            if (parentLink != null) {
                return parentLink.getHref();
            }
        }
        return null;
    }
}
