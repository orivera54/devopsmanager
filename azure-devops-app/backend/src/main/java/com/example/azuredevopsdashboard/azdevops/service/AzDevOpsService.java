package com.example.azuredevopsdashboard.azdevops.service;

import com.example.azuredevopsdashboard.azdevops.client.AzureDevOpsClientService;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectDto;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectListResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AzDevOpsService {

    private static final Logger logger = LoggerFactory.getLogger(AzDevOpsService.class);
    private final AzureDevOpsClientService azureDevOpsClientService;

    @Autowired
    public AzDevOpsService(AzureDevOpsClientService azureDevOpsClientService) {
        this.azureDevOpsClientService = azureDevOpsClientService;
    }

    public List<AzDevOpsProjectDto> listProjects() {
        logger.debug("Attempting to list Azure DevOps projects via AzDevOpsService.");
        AzDevOpsProjectListResponseDto response = azureDevOpsClientService.getProjects();

        if (response != null && response.getValue() != null) {
            // Log the number of projects if the response and value list are not null
            logger.info("Successfully fetched {} projects from Azure DevOps.", response.getValue().size());
            // It's also good practice to check response.getCount() if it's expected to match response.getValue().size()
            // logger.info("API reported count: {}", response.getCount());
            return response.getValue();
        }

        logger.warn("No projects found or an error occurred while fetching from Azure DevOps through client service.");
        return Collections.emptyList();
    }
}
