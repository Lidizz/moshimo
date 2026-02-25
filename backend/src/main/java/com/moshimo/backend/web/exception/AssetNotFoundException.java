package com.moshimo.backend.web.exception;

/**
 * Exception thrown when an asset is not found.
 * 
 * Learning: Custom exceptions for domain-specific error handling.
 */
public class AssetNotFoundException extends RuntimeException {
    
    public AssetNotFoundException(String message) {
        super(message);
    }
}