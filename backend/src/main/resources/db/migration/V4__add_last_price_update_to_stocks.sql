-- V4: Add last_price_update column to stocks table
-- Purpose: Track when price data was last updated from external API

ALTER TABLE stocks
ADD COLUMN last_price_update DATE;

COMMENT ON COLUMN stocks.last_price_update IS 'Date when price data was last updated from external API (Twelve Data, etc.)';
