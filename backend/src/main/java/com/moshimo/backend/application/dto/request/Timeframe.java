package com.moshimo.backend.application.dto.request;

/**
 * Timeline aggregation granularity for portfolio performance charts.
 * 
 * Learning: Enums for finite set of options (type-safe, self-documenting)
 * Pattern: Value object for domain concepts
 * 
 * Educational Context:
 * This enum teaches the concept of data aggregation in financial analysis.
 * Different timeframes reveal different aspects of investment performance:
 * - Daily: Shows volatility and short-term noise
 * - Weekly: Smooths daily fluctuations, shows trends
 * - Monthly: Clear long-term patterns
 * - Yearly: Maximum zoom-out for decade+ perspectives
 * - ALL: Balances detail with performance
 */
public enum Timeframe {
    /**
     * Daily granularity - shows every trading day.
     * Use case: Short periods (<3 months) or detailed analysis.
     * Data points: ~250 per year
     * 
     * Educational value: Reveals market volatility, teaches that daily
     * fluctuations are normal and should be ignored by long-term investors.
     */
    ONE_DAY("1D"),
    
    /**
     * Weekly granularity - first trading day of each week (Monday).
     * Use case: Medium periods (3-12 months), reduces daily noise.
     * Data points: ~52 per year
     * 
     * Educational value: Shows weekly trends while filtering out
     * daily panic/euphoria cycles.
     */
    ONE_WEEK("1W"),
    
    /**
     * Monthly granularity - first trading day of each month.
     * Use case: Long periods (1-5 years), clear trend visualization.
     * Data points: 12 per year
     * 
     * Educational value: Monthly view helps students see quarterly
     * earnings impacts, seasonal patterns, and medium-term trends.
     */
    ONE_MONTH("1M"),
    
    /**
     * Yearly granularity - first trading day of January each year.
     * Use case: Very long periods (10+ years), maximum zoom-out.
     * Data points: 1 per year
     * 
     * Educational value: Demonstrates the power of long-term compounding.
     * At this scale, even major crashes (2008, 2020) become minor dips
     * in the overall upward trajectory of quality investments.
     */
    ONE_YEAR("1Y"),
    
    /**
     * Auto-sampling - intelligently samples to max ~500 points.
     * Algorithm: If totalDays > 500, sample every Nth day where N = totalDays / 500.
     * Use case: Default option, balances detail with performance.
     * 
     * Educational value: Shows complete picture without overwhelming
     * browser with thousands of data points.
     */
    ALL("ALL");
    
    private final String code;
    
    Timeframe(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Parse from string code (case-insensitive).
     * @param code "1D", "1W", "1M", "1Y", or "ALL"
     * @return corresponding Timeframe
     * @throws IllegalArgumentException if code invalid
     */
    public static Timeframe fromCode(String code) {
        if (code == null) {
            return ALL; // Default
        }
        
        for (Timeframe tf : values()) {
            if (tf.code.equalsIgnoreCase(code)) {
                return tf;
            }
        }
        throw new IllegalArgumentException("Invalid timeframe code: " + code + 
            ". Valid values: 1D, 1W, 1M, 1Y, ALL");
    }
}
