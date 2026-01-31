package com.moshimo.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global Web MVC Configuration.
 * 
 * Configures CORS (Cross-Origin Resource Sharing) to allow the React frontend
 * running on localhost:5173 to communicate with the Spring Boot backend on localhost:8080.
 * 
 * Learning Notes:
 * - WebMvcConfigurer provides callback methods to customize Spring MVC configuration
 * - CORS is required because browser's Same-Origin Policy blocks cross-origin requests
 * - In production, restrict allowedOrigins to your actual domain
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS mappings for all endpoints.
     * 
     * @param registry the CORS registry to add mappings to
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Apply to all API endpoints
                .allowedOrigins(
                        "http://localhost:5173",      // Vite dev server
                        "http://127.0.0.1:5173"       // Alternative localhost
                )
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*")                   // Allow all headers
                .allowCredentials(true)                // Allow cookies/auth headers
                .maxAge(3600);                         // Cache preflight response for 1 hour
    }
}