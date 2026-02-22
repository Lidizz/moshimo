package com.moshimo.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * API Client Configuration - Beans for external API integration.
 *
 * Learning Notes:
 * - RestTemplate: Synchronous HTTP client (Spring classic)
 * - @Bean: Register in Spring IoC container
 * - Singleton scope by default (one instance shared)
 *
 * Note: ObjectMapper is intentionally NOT defined here.
 * Spring Boot auto-configures ObjectMapper with Java Time module support,
 * correct LocalDate serialization, and all application.yml jackson settings.
 * Defining a plain `new ObjectMapper()` here would override that and lose
 * all Spring Boot defaults.
 */
@Configuration
public class ApiClientConfig {

    /**
     * RestTemplate for making HTTP requests to stock data APIs.
     *
     * Note: RestTemplate is in maintenance mode. Consider RestClient
     * (Spring 6.1+) for new projects, but RestTemplate is simpler for learning.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
