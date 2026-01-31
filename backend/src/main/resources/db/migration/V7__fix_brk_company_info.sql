-- Fix BRK.B company information (was auto-imported with placeholder data)
UPDATE stocks 
SET name = 'Berkshire Hathaway Inc.',
    sector = 'Financial Services',
    industry = 'Insurance - Diversified',
    exchange = 'NYSE'
WHERE symbol = 'BRK.B';
