-- V8: Enrich metadata for stocks imported via TwelveData API before auto-enrichment was added.
-- These were created as "SYMBOL (Auto-imported)" placeholders with no sector/industry/exchange.
-- Future imports auto-fetch this data via Twelve Data /profile endpoint.

-- Broadcom Inc. (semiconductor company, designs chips for networking, storage, wireless)
UPDATE stocks SET
    name = 'Broadcom Inc.',
    sector = 'Technology',
    industry = 'Semiconductors',
    exchange = 'NASDAQ',
    asset_type = 'STOCK'
WHERE symbol = 'AVGO' AND name LIKE '%(Auto-imported)%';

-- Oracle Corporation (enterprise software, cloud infrastructure, databases)
UPDATE stocks SET
    name = 'Oracle Corporation',
    sector = 'Technology',
    industry = 'Software - Infrastructure',
    exchange = 'NYSE',
    asset_type = 'STOCK'
WHERE symbol = 'ORCL' AND name LIKE '%(Auto-imported)%';

-- Salesforce, Inc. (cloud-based CRM and enterprise software)
UPDATE stocks SET
    name = 'Salesforce, Inc.',
    sector = 'Technology',
    industry = 'Software - Application',
    exchange = 'NYSE',
    asset_type = 'STOCK'
WHERE symbol = 'CRM' AND name LIKE '%(Auto-imported)%';
