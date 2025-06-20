package com.example.azuredevopsdashboard.azdevops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HrefDto {
    private String href;

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }
}
