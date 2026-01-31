-- V2: Create stock_prices table for historical price data
-- Purpose: Store daily OHLCV (Open, High, Low, Close, Volume) data
-- Learning: This will be the largest table - query optimization is critical
-- Expected size: ~100,000 rows for 20 stocks × 20 years × ~250 trading days/year

CREATE TABLE stock_prices (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL REFERENCES stocks(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    open NUMERIC(12, 4) NOT NULL,
    high NUMERIC(12, 4) NOT NULL,
    low NUMERIC(12, 4) NOT NULL,
    close NUMERIC(12, 4) NOT NULL,
    volume BIGINT,
    adjusted_close NUMERIC(12, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Ensure one price record per stock per day
    CONSTRAINT uq_stock_price_date UNIQUE(stock_id, date),
    
    -- Data integrity constraints
    CONSTRAINT chk_prices_positive CHECK (open > 0 AND high > 0 AND low > 0 AND close > 0),
    CONSTRAINT chk_high_low CHECK (high >= low),
    CONSTRAINT chk_volume_positive CHECK (volume IS NULL OR volume >= 0)
);

-- CRITICAL PERFORMANCE INDEXES
-- Learning: Composite index on (stock_id, date DESC) enables fast range queries
-- Pattern: WHERE stock_id = ? AND date BETWEEN ? AND ? ORDER BY date DESC
CREATE INDEX idx_stock_prices_stock_date ON stock_prices(stock_id, date DESC);

-- Additional index for date-based queries (e.g., "what was trading on 2020-01-01?")
CREATE INDEX idx_stock_prices_date ON stock_prices(date);

-- Add comments for documentation
COMMENT ON TABLE stock_prices IS 'Historical daily price data for stocks - optimized for time-series queries';
COMMENT ON COLUMN stock_prices.close IS 'Closing price for the trading day';
COMMENT ON COLUMN stock_prices.adjusted_close IS 'Close price adjusted for splits and dividends - use for accurate return calculations';
COMMENT ON CONSTRAINT uq_stock_price_date ON stock_prices IS 'Prevents duplicate price entries for the same stock on the same date';