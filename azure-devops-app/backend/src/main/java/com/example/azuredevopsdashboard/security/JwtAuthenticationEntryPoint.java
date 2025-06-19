package com.example.azuredevopsdashboard.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {
        logger.error("Responding with unauthorized error. Message - {}. Path - {}", e.getMessage(), httpServletRequest.getRequestURI());
        // Consider sending a JSON response for API clients
        // For example:
        // httpServletResponse.setContentType("application/json");
        // httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // httpServletResponse.getOutputStream().println("{ \"error\": \"" + e.getMessage() + "\" }");
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    }
}
