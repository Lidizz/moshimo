-- Fix BRK-B symbol to match TwelveData format (BRK.B)
-- TwelveData uses periods for share class notation, not hyphens

-- First, check if BRK.B already exists to avoid duplicate key violation
-- If BRK.B exists and BRK-B also exists, delete BRK-B (and its prices via CASCADE)
DELETE FROM stocks 
WHERE symbol = 'BRK-B' 
  AND EXISTS (SELECT 1 FROM stocks WHERE symbol = 'BRK.B');

-- Update BRK-B to BRK.B if it still exists (no duplicate)
UPDATE stocks 
SET symbol = 'BRK.B' 
WHERE symbol = 'BRK-B';
