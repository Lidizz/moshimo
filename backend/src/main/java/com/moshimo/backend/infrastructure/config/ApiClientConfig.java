package com.moshimo.backend.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * API Client Configuration - Beans for external API integration.
 * 
 * Learning Notes:
 * - RestTemplate: Synchronous HTTP client (Spring classic)
 * - ObjectMapper: Jackson JSON parser/serializer
 * - @Bean: Register in Spring IoC container
 * - Singleton scope by default (one instance shared)
 */
@Configuration
public class ApiClientConfig {

    /**
     * RestTemplate for making HTTP requests to stock data APIs.
     * 
     * Note: RestTemplate is in maintenance mode. Consider WebClient
     * for new projects, but RestTemplate is simpler for learning.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper for JSON parsing.
     * Spring Boot auto-configures this, but explicit bean for clarity.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
