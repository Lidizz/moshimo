package com.moshimo.backend.web.controller;

import com.moshimo.backend.domain.repository.StockRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestDataController {

    private final StockRepository stockRepository;

    public TestDataController(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @GetMapping("/api/test/stocks")
    public List<?> getStocks() {
        return stockRepository.findAll();
    }

    @GetMapping("/api/test/active")
    public List<?> getActiveStocks() {
        return stockRepository.findByIsActiveTrue();
    }
}