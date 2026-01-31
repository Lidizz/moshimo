-- V3: Insert initial 20 MVP stocks for testing
-- Purpose: Seed database with popular stocks for MVP phase
-- Learning: These stocks have extensive historical data in Kaggle dataset
-- Source: PROJECT_BRIEF.md - selected for liquidity, data availability, and popularity

-- ETFs: Market benchmarks
INSERT INTO stocks (symbol, name, sector, exchange, is_active) VALUES
('SPY', 'SPDR S&P 500 ETF Trust', 'ETF', 'NYSE', true),
('QQQ', 'Invesco QQQ Trust', 'ETF', 'NASDAQ', true);

-- Technology Giants
INSERT INTO stocks (symbol, name, sector, exchange, is_active) VALUES
('GOOGL', 'Alphabet Inc.', 'Technology', 'NASDAQ', true),
('AAPL', 'Apple Inc.', 'Technology', 'NASDAQ', true),
('MSFT', 'Microsoft Corporation', 'Technology', 'NASDAQ', true),
('NVDA', 'NVIDIA Corporation', 'Technology', 'NASDAQ', true),
('TSLA', 'Tesla, Inc.', 'Automotive', 'NASDAQ', true),
('AMZN', 'Amazon.com, Inc.', 'Technology', 'NASDAQ', true),
('META', 'Meta Platforms, Inc.', 'Technology', 'NASDAQ', true);

-- Finance & Services
INSERT INTO stocks (symbol, name, sector, exchange, is_active) VALUES
('BRK-B', 'Berkshire Hathaway Inc.', 'Financial Services', 'NYSE', true),
('JPM', 'JPMorgan Chase & Co.', 'Financial Services', 'NYSE', true),
('V', 'Visa Inc.', 'Financial Services', 'NYSE', true),
('MA', 'Mastercard Incorporated', 'Financial Services', 'NYSE', true);

-- Consumer & Retail
INSERT INTO stocks (symbol, name, sector, exchange, is_active) VALUES
('WMT', 'Walmart Inc.', 'Consumer Defensive', 'NYSE', true),
('HD', 'The Home Depot, Inc.', 'Consumer Cyclical', 'NYSE', true);

-- Healthcare & Pharmaceuticals
INSERT INTO stocks (symbol, name, sector, exchange, is_active) VALUES
('JNJ', 'Johnson & Johnson', 'Healthcare', 'NYSE', true),
('UNH', 'UnitedHealth Group Incorporated', 'Healthcare', 'NYSE', true);

-- Consumer Staples
INSERT INTO stocks (symbol, name, sector, exchange, is_active) VALUES
('PG', 'The Procter & Gamble Company', 'Consumer Defensive', 'NYSE', true);

-- Energy
INSERT INTO stocks (symbol, name, sector, exchange, is_active) VALUES
('XOM', 'Exxon Mobil Corporation', 'Energy', 'NYSE', true),
('CVX', 'Chevron Corporation', 'Energy', 'NYSE', true);

-- Verify insertion
-- Expected: 20 stocks total
