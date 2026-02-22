package com.moshimo.backend.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
 * Note on ObjectMapper: Spring Boot 4.x uses tools.jackson (Jackson 3.x) for
 * its internal MVC serialization. TwelveDataClient and AlphaVantageClient use
 * the legacy com.fasterxml.jackson 2.x API (readTree) to parse external API
 * responses. This bean satisfies their constructor dependency.
 * JavaTimeModule is intentionally omitted — this mapper is for API response
 * parsing only, not for serializing Java time types.
 */
@Configuration
public class ApiClientConfig {

    /**
     * ObjectMapper for parsing external API JSON responses (readTree only).
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

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

