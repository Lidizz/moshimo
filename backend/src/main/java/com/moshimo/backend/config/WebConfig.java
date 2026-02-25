package com.moshimo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global Web MVC Configuration.
 * 
 * Configures CORS (Cross-Origin Resource Sharing) to allow the frontend
 * to communicate with the Spring Boot backend across origins.
 * 
 * Allowed origins are configured per profile:
 * - dev: localhost:5173 (Vite dev server) — see application-dev.yml
 * - prod: real domain from CORS_ALLOWED_ORIGINS env var — see application-prod.yml
 * 
 * Learning Notes:
 * - WebMvcConfigurer provides callback methods to customize Spring MVC configuration
 * - CORS is required because browser's Same-Origin Policy blocks cross-origin requests
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Configure CORS mappings for all endpoints.
     * 
     * @param registry the CORS registry to add mappings to
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}