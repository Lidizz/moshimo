package com.moshimo.backend.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Health Check Response DTO using Java Record.
 * 
 * Learning Notes - Java Records (Java 14+, standardized in Java 16):
 * - Records are immutable data carriers (perfect for DTOs)
 * - Automatically generates: constructor, getters, equals(), hashCode(), toString()
 * - Compact syntax: No need for @Data, @Builder, etc.
 * - Thread-safe by default (immutability)
 * - Pattern matching support (Java 25 feature)
 * 
 * Why use records for DTOs?
 * - DTOs should be immutable (data transfer, not modification)
 * - Less boilerplate than traditional classes
 * - Clear intent: "This is just data"
 * - Better performance (JVM optimizations)
 * 
 * Syntax: record Name(Type field1, Type field2) { }
 */
public record HealthResponse(
    @JsonProperty("status")
    String status,
    
    @JsonProperty("timestamp")
    LocalDateTime timestamp,
    
    @JsonProperty("database")
    DatabaseInfo database,
    
    @JsonProperty("application")
    ApplicationInfo application
) {
    /**
     * Nested record for database information.
     */
    public record DatabaseInfo(
        @JsonProperty("connected")
        boolean connected,
        
        @JsonProperty("version")
        String version,
        
        @JsonProperty("totalStocks")
        long totalStocks,
        
        @JsonProperty("totalPriceRecords")
        long totalPriceRecords
    ) {}
    
    /**
     * Nested record for application information.
     */
    public record ApplicationInfo(
        @JsonProperty("name")
        String name,
        
        @JsonProperty("version")
        String version,
        
        @JsonProperty("environment")
        String environment
    ) {}
    
    /**
     * Factory method for successful health check.
     * 
     * Learning: Static factory methods provide readable object creation.
     */
    public static HealthResponse healthy(
        String dbVersion,
        long stockCount,
        long priceCount,
        String appName,
        String appVersion,
        String environment
    ) {
        return new HealthResponse(
            "UP",
            LocalDateTime.now(),
            new DatabaseInfo(true, dbVersion, stockCount, priceCount),
            new ApplicationInfo(appName, appVersion, environment)
        );
    }
    
    /**
     * Factory method for failed health check.
     */
    public static HealthResponse unhealthy(String errorMessage) {
        return new HealthResponse(
            "DOWN",
            LocalDateTime.now(),
            new DatabaseInfo(false, errorMessage, 0L, 0L),
            new ApplicationInfo("moshimo-backend", "1.0.0", "unknown")
        );
    }
}