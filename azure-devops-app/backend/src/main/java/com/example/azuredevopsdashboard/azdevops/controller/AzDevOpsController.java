package com.example.azuredevopsdashboard.azdevops.controller;

import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectDto;
import com.example.azuredevopsdashboard.azdevops.service.AzDevOpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/azdevops")
public class AzDevOpsController {

    private static final Logger logger = LoggerFactory.getLogger(AzDevOpsController.class);
    private final AzDevOpsService azDevOpsService;

    @Autowired
    public AzDevOpsController(AzDevOpsService azDevOpsService) {
        this.azDevOpsService = azDevOpsService;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<AzDevOpsProjectDto>> getProjects() {
        logger.info("Received request for listing Azure DevOps projects.");
        try {
            List<AzDevOpsProjectDto> projects = azDevOpsService.listProjects();
            if (projects.isEmpty()) {
                // Consider if NO_CONTENT is more appropriate if the service intentionally returns empty
                // based on some business logic (e.g. user has no projects).
                // If it implies an issue or simply no data from AzDO, OK with empty list is fine.
                logger.info("No Azure DevOps projects found or returned by service.");
                return ResponseEntity.ok(projects); // Returns an empty list with 200 OK
            }
            logger.info("Returning {} Azure DevOps projects.", projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            // This catch block might be too generic. Specific exceptions should be handled if thrown by the service.
            // For now, it ensures any unexpected service layer error results in a 500.
            logger.error("An unexpected error occurred while retrieving Azure DevOps projects.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
