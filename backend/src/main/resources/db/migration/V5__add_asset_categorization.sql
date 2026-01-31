-- V5__add_asset_categorization.sql
-- Add asset type column for categorizing stocks, ETFs, and indexes
-- Ticket #2: Asset Category System (Backend)
-- Date: January 31, 2026

-- Add asset_type column with default value 'STOCK'
ALTER TABLE stocks 
ADD COLUMN IF NOT EXISTS asset_type VARCHAR(20) NOT NULL DEFAULT 'STOCK';

-- Create index for asset_type filtering
CREATE INDEX IF NOT EXISTS idx_stocks_asset_type ON stocks(asset_type);

-- Update known ETFs based on existing data
-- SPY and QQQ are ETFs
UPDATE stocks SET asset_type = 'ETF' 
WHERE symbol IN ('SPY', 'QQQ');

-- Update based on name patterns (Trust, Fund typically indicate ETFs)
UPDATE stocks SET asset_type = 'ETF' 
WHERE asset_type = 'STOCK'
  AND (
    LOWER(name) LIKE '%etf%'
    OR LOWER(name) LIKE '%trust%'
    OR LOWER(name) LIKE '%fund%'
    OR LOWER(name) LIKE '%spdr%'
    OR LOWER(name) LIKE '%ishares%'
    OR LOWER(name) LIKE '%vanguard%'
    OR LOWER(name) LIKE '%invesco%'
  );

-- Add comment for documentation
COMMENT ON COLUMN stocks.asset_type IS 'Asset type: STOCK, ETF, or INDEX';
