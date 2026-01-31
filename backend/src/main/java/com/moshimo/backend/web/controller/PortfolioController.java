package com.moshimo.backend.web.controller;

import com.moshimo.backend.application.dto.request.SimulationRequest;
import com.moshimo.backend.application.dto.response.SimulationResponse;
import com.moshimo.backend.domain.service.InvestmentSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Portfolio Controller - REST API for investment simulations.
 * 
 * Learning Notes:
 * - @Valid: Triggers validation on request body using Bean Validation
 * - @RequestBody: Deserializes JSON request body to Java object
 * - POST: Used for operations that process data (simulation calculations)
 * 
 * Design Pattern: Controller Pattern, Command Pattern (simulation request)
 */
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final InvestmentSimulationService simulationService;

    /**
     * Simulate investment portfolio performance.
     * 
     * POST /api/portfolio/simulate
     * 
     * Request Body:
     * {
     *   "investments": [
     *     {
     *       "symbol": "AAPL",
     *       "amountUsd": 1000,
     *       "purchaseDate": "2020-01-01"
     *     }
     *   ],
     *   "endDate": "2024-12-31"
     * }
     * 
     * Response:
     * {
     *   "totalInvested": 1000.00,
     *   "currentValue": 3245.67,
     *   "absoluteGain": 2245.67,
     *   "percentReturn": 224.57,
     *   "cagr": 34.25,
     *   "timeline": [...],
     *   "holdings": [...]
     * }
     * 
     * @param request simulation parameters (validated)
     * @return simulation results
     */
    @PostMapping("/simulate")
    public ResponseEntity<SimulationResponse> simulate(@Valid @RequestBody SimulationRequest request) {
        log.info("POST /api/portfolio/simulate - Starting simulation with {} investments", 
                 request.getInvestments().size());
        
        SimulationResponse response = simulationService.simulate(request);
        
        log.info("Simulation complete - Total invested: {}, Current value: {}, Return: {}%",
                 response.totalInvested(), response.currentValue(), response.percentReturn());
        
        return ResponseEntity.ok(response);
    }
}