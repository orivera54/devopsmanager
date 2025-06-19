package com.example.azuredevopsdashboard.azdevops.client;

import com.example.azuredevopsdashboard.azdevops.config.AzureDevOpsConfig;
import com.example.azuredevopsdashboard.azdevops.dto.AzDevOpsProjectListResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Service
public class AzureDevOpsClientService {

    private static final Logger logger = LoggerFactory.getLogger(AzureDevOpsClientService.class);

    private final RestTemplate restTemplate;
    private final AzureDevOpsConfig azureDevOpsConfig;

    @Autowired
    public AzureDevOpsClientService(RestTemplate restTemplate, AzureDevOpsConfig azureDevOpsConfig) {
        this.restTemplate = restTemplate;
        this.azureDevOpsConfig = azureDevOpsConfig;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String pat = azureDevOpsConfig.getPersonalAccessToken();
        if (StringUtils.hasText(pat)) {
            String auth = ":" + pat; // Username is empty for PAT
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        } else {
            logger.warn("Azure DevOps PAT is not configured. Requests to Azure DevOps will likely fail.");
            // Depending on Azure DevOps instance configuration, unauthenticated requests might still return some public data
            // or, more likely, fail. For this example, we proceed without Authorization header if PAT is missing.
        }
        return headers;
    }

    public AzDevOpsProjectListResponseDto getProjects() {
        if (!StringUtils.hasText(azureDevOpsConfig.getOrganizationUrl())) {
            logger.error("Azure DevOps Organization URL is not configured. Cannot fetch projects.");
            return null; // Or throw a custom configuration exception
        }
        // PAT absence is handled in createHeaders, but you might want to check here too if it's strictly required

        String url = String.format("%s/_apis/projects?api-version=%s",
                                   azureDevOpsConfig.getOrganizationUrl(),
                                   azureDevOpsConfig.getApiVersion());
        logger.info("Fetching projects from Azure DevOps URL: {}", url);

        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<AzDevOpsProjectListResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AzDevOpsProjectListResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                 logger.debug("Successfully fetched projects. Count: {}", response.getBody() != null ? response.getBody().getCount() : "N/A");
                return response.getBody();
            } else {
                logger.error("Failed to fetch projects. Status code: {}", response.getStatusCode());
                return null;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error fetching projects from Azure DevOps: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error fetching projects from Azure DevOps: {}", e.getMessage(), e);
            return null;
        }
    }
}
