package com.moshimo.backend.web.controller;

import com.moshimo.backend.application.dto.response.HealthResponse;
import com.moshimo.backend.domain.repository.StockPriceRepository;
import com.moshimo.backend.domain.repository.StockRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check Controller - System status and diagnostics endpoint.
 * 
 * Learning Notes:
 * - @RestController: Combines @Controller + @ResponseBody (returns JSON by default)
 * - @RequestMapping: Base path for all endpoints in this controller
 * - @RequiredArgsConstructor: Lombok generates constructor for final fields (DI)
 * - @Slf4j: Lombok generates logger instance (log.info(), log.error(), etc.)
 * - EntityManager: JPA interface for executing native SQL queries
 * - @Value: Injects properties from application.yml
 * 
 * Design Pattern: Controller Pattern (handles HTTP requests/responses)
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // Inject application properties
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Health check endpoint - Returns system status and database statistics.
     * 
     * GET /api/health
     * 
     * Response Example:
     * {
     *   "status": "UP",
     *   "timestamp": "2026-01-01T10:30:00",
     *   "database": {
     *     "connected": true,
     *     "version": "PostgreSQL 15.5",
     *     "totalStocks": 20,
     *     "totalPriceRecords": 0
     *   },
     *   "application": {
     *     "name": "moshimo-backend",
     *     "version": "1.0.0",
     *     "environment": "dev"
     *   }
     * }
     * 
     * @return ResponseEntity with health status (200 OK if healthy, 503 if down)
     */
    @GetMapping
    public ResponseEntity<HealthResponse> checkHealth() {
        log.debug("Health check requested");
        
        try {
            // Query database connection and version
            String dbVersion = getDatabaseVersion();
            
            // Count entities (demonstrates repository usage)
            long stockCount = stockRepository.count();
            long priceCount = stockPriceRepository.count();
            
            log.info("Health check successful - DB: {}, Stocks: {}, Prices: {}", 
                     dbVersion, stockCount, priceCount);
            
            // Build healthy response using factory method
            HealthResponse response = HealthResponse.healthy(
                dbVersion,
                stockCount,
                priceCount,
                applicationName,
                "1.0.0", // TODO: Read from build metadata or manifest
                activeProfile
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            
            // Return 503 Service Unavailable with error details
            HealthResponse response = HealthResponse.unhealthy(e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Query PostgreSQL version using native SQL.
     * 
     * Learning Notes:
     * - createNativeQuery: Execute raw SQL (use sparingly, prefer JPQL)
     * - getSingleResult: Returns one result (throws exception if 0 or multiple)
     * - Cast to String: Native queries return Object by default
     * 
     * @return PostgreSQL version string (e.g., "PostgreSQL 15.5")
     */
    private String getDatabaseVersion() {
        try {
            Object result = entityManager
                .createNativeQuery("SELECT version()")
                .getSingleResult();
            
            // PostgreSQL version() returns format: "PostgreSQL 15.5 on x86_64-pc-linux-gnu..."
            // Extract just the first part for cleaner display
            String fullVersion = result.toString();
            int commaIndex = fullVersion.indexOf(',');
            if (commaIndex > 0) {
                return fullVersion.substring(0, commaIndex).trim();
            }
            return fullVersion;
            
        } catch (Exception e) {
            log.warn("Could not retrieve database version", e);
            return "Unknown";
        }
    }
}