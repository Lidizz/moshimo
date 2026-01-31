package com.moshimo.backend.domain.model;

/**
 * AssetType Enum - Categorizes tradeable assets.
 * 
 * Learning Notes:
 * - Enums are type-safe constants in Java
 * - @Enumerated(EnumType.STRING) in JPA stores the name, not ordinal
 * - Adding new values is safe when using STRING storage (no ordinal shift)
 * 
 * Future: CRYPTO type planned for Phase 2
 */
public enum AssetType {
    
    /**
     * Individual company stocks (e.g., AAPL, GOOGL, MSFT)
     */
    STOCK,
    
    /**
     * Exchange-Traded Funds (e.g., SPY, QQQ, VOO)
     * ETFs track indexes, sectors, or baskets of assets
     */
    ETF,
    
    /**
     * Market indexes (e.g., ^GSPC for S&P 500, ^DJI for Dow Jones)
     * Note: Some indexes are not directly tradeable
     */
    INDEX;
    
    /**
     * Infer asset type from symbol pattern or name.
     * 
     * Simple heuristics:
     * - Symbols starting with ^ are typically indexes
     * - Common ETF patterns detected by known suffixes
     * 
     * @param symbol the stock/ETF ticker symbol
     * @param name the full name (optional, can be null)
     * @return inferred AssetType, defaults to STOCK
     */
    public static AssetType inferFromSymbol(String symbol, String name) {
        if (symbol == null || symbol.isBlank()) {
            return STOCK;
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        String lowerName = name != null ? name.toLowerCase() : "";
        
        // Index detection: symbols starting with ^
        if (upperSymbol.startsWith("^")) {
            return INDEX;
        }
        
        // ETF detection by common patterns
        if (lowerName.contains("etf") || 
            lowerName.contains("trust") ||
            lowerName.contains("fund") ||
            lowerName.contains("spdr") ||
            lowerName.contains("ishares") ||
            lowerName.contains("vanguard") ||
            lowerName.contains("invesco")) {
            return ETF;
        }
        
        // Known ETF symbols (common ones)
        if (isKnownEtf(upperSymbol)) {
            return ETF;
        }
        
        // Default to stock
        return STOCK;
    }
    
    /**
     * Check if symbol is a well-known ETF.
     * Uses Java 25 switch expression with pattern matching.
     */
    private static boolean isKnownEtf(String symbol) {
        return switch (symbol) {
            case "SPY", "QQQ", "VOO", "VTI", "IVV", "IWM", "EEM", "VEA", "VWO",
                 "AGG", "BND", "TLT", "GLD", "SLV", "USO", "VNQ", "XLF", "XLK",
                 "XLE", "XLV", "XLI", "XLY", "XLP", "XLU", "XLB", "XLRE",
                 "DIA", "MDY", "IJH", "IJR", "ARKK", "ARKW", "ARKG", "ARKF",
                 "SCHD", "VYM", "VIG", "DGRO" -> true;
            default -> false;
        };
    }
}
