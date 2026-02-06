package com.example.moneytracker.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    /**
     * Allowed origins (e.g. https://app.example.com). Default allows localhost for dev.
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:8080");

    /**
     * Allowed HTTP methods.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /**
     * Allowed headers (Authorization, Content-Type, etc).
     */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Accept");

    /**
     * Max age for preflight cache (seconds).
     */
    private long maxAgeSeconds = 3600;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }
}
