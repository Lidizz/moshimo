package com.moshimo.backend.web.exception;

/**
 * Exception thrown when a stock is not found.
 * 
 * Learning: Custom exceptions for domain-specific error handling.
 */
public class StockNotFoundException extends RuntimeException {
    
    public StockNotFoundException(String message) {
        super(message);
    }
}