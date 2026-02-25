-- V9: Rename stocks/stock_prices tables to asset/asset_price
-- The application manages stocks, ETFs, indexes, and (future) crypto under a unified
-- "asset" domain model.  Renaming the tables, column, indexes, and constraints now
-- avoids a misleading schema as the asset catalogue grows.
--
-- All operations are metadata-only (no data copy) → instant on any table size.

-- 1. Rename tables
ALTER TABLE stocks RENAME TO asset;
ALTER TABLE stock_prices RENAME TO asset_price;

-- 2. Rename FK column (stock_id → asset_id)
ALTER TABLE asset_price RENAME COLUMN stock_id TO asset_id;

-- 3. Rename indexes to match new table names
ALTER INDEX idx_stocks_symbol        RENAME TO idx_asset_symbol;
ALTER INDEX idx_stocks_sector        RENAME TO idx_asset_sector;
ALTER INDEX idx_stocks_is_active     RENAME TO idx_asset_is_active;
ALTER INDEX idx_stocks_asset_type    RENAME TO idx_asset_asset_type;
ALTER INDEX idx_stock_prices_stock_date RENAME TO idx_asset_price_asset_date;
ALTER INDEX idx_stock_prices_date    RENAME TO idx_asset_price_date;

-- 4. Rename named constraints
ALTER TABLE asset_price RENAME CONSTRAINT uq_stock_price_date TO uq_asset_price_date;

-- 5. Update comments
COMMENT ON TABLE asset IS 'Master table of tradeable assets (stocks, ETFs, indexes, crypto) for investment simulation';
COMMENT ON COLUMN asset.symbol IS 'Ticker symbol (e.g., AAPL, SPY, BTC-USD)';
COMMENT ON COLUMN asset.ipo_date IS 'Listing/inception date — prevents simulation before this date';
COMMENT ON COLUMN asset.is_active IS 'Whether the asset is currently active and should appear in UI';
COMMENT ON COLUMN asset.asset_type IS 'Asset category: STOCK, ETF, INDEX, or CRYPTO';

COMMENT ON TABLE asset_price IS 'Historical daily price data for assets — optimised for time-series queries';
COMMENT ON COLUMN asset_price.close IS 'Closing price for the trading day';
COMMENT ON COLUMN asset_price.adjusted_close IS 'Close price adjusted for splits and dividends — use for accurate return calculations';
COMMENT ON CONSTRAINT uq_asset_price_date ON asset_price IS 'Prevents duplicate price entries for the same asset on the same date';
