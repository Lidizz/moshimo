import type { HoldingInfo, InvestmentItem } from '../types/api.types';
import './PortfolioHeader.css';

interface PortfolioHeaderProps {
  holdings: HoldingInfo[];
  investments: InvestmentItem[];
  endDate: string; // Format: YYYY-MM-DD
}

/**
 * Portfolio Header Component - Shows date range and asset coverage.
 * 
 * Educational: Helps students understand the full time horizon of their investment.
 */
export function PortfolioHeader({ holdings, investments, endDate }: PortfolioHeaderProps) {
  // Find earliest purchase date
  const purchaseDates = investments.map(inv => new Date(inv.purchaseDate));
  const earliestDate = new Date(Math.min(...purchaseDates.map(d => d.getTime())));
  const latestDate = new Date(endDate);

  // Calculate years
  const yearsDiff = (latestDate.getTime() - earliestDate.getTime()) / (1000 * 60 * 60 * 24 * 365.25);

  // Format dates
  const formatDate = (date: Date) => {
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric' 
    });
  };

  // Group holdings with their IPO info
  const assetInfo = holdings.map(h => {
    const investment = investments.find(inv => inv.symbol === h.symbol);
    return {
      symbol: h.symbol,
      name: h.name,
      purchaseDate: investment?.purchaseDate || '',
    };
  });

  return (
    <div className="portfolio-header">
      <div className="portfolio-header__main">
        <span className="portfolio-header__icon">📊</span>
        <span className="portfolio-header__text">
          <strong>Portfolio Performance:</strong> {formatDate(earliestDate)} → {formatDate(latestDate)}
        </span>
        <span className="portfolio-header__duration">
          ({yearsDiff.toFixed(1)} years)
        </span>
      </div>
      
      <div className="portfolio-header__assets">
        <strong>Data Coverage:</strong>
        {assetInfo.map((asset, idx) => (
          <span key={asset.symbol} className="portfolio-header__asset">
            {idx > 0 && ' • '}
            {asset.symbol} ({asset.name})
          </span>
        ))}
      </div>
    </div>
  );
}
