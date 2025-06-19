package com.example.azuredevopsdashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.web.client.RestTemplateBuilder; // Added import
import org.springframework.context.annotation.Bean; // Added import
import org.springframework.web.client.RestTemplate; // Added import

@SpringBootApplication
public class AzureDevopsDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureDevopsDashboardApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
