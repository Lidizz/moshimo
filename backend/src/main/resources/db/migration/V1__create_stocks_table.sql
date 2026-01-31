-- V1: Create stocks master table
-- Purpose: Store basic information about available stocks/ETFs
-- Learning: BIGSERIAL for auto-incrementing IDs, proper indexing for performance

CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    sector VARCHAR(100),
    industry VARCHAR(100),
    exchange VARCHAR(50),
    ipo_date DATE,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Performance indexes
-- Learning: Index on symbol for O(1) lookups, sector for filtering
CREATE INDEX idx_stocks_symbol ON stocks(symbol);
CREATE INDEX idx_stocks_sector ON stocks(sector) WHERE sector IS NOT NULL;
CREATE INDEX idx_stocks_is_active ON stocks(is_active);

-- Add comment for documentation
COMMENT ON TABLE stocks IS 'Master table of available stocks and ETFs for investment simulation';
COMMENT ON COLUMN stocks.symbol IS 'Stock ticker symbol (e.g., AAPL, GOOGL)';
COMMENT ON COLUMN stocks.ipo_date IS 'Initial Public Offering date - prevents simulation before this date';
COMMENT ON COLUMN stocks.is_active IS 'Whether the stock is currently tradeable and should appear in UI';