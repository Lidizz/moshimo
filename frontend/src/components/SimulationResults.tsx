import { PortfolioChart } from './PortfolioChart';
import { PortfolioHeader } from './PortfolioHeader';
import { useCountUp } from '../hooks/useCountUp';
import type { SimulationResponse, InvestmentItem } from '../types/api.types';
import './SimulationResults.css';

interface SimulationResultsProps {
  results: SimulationResponse;
  investments?: InvestmentItem[];
}

/**
 * Simulation Results Component - Displays portfolio performance metrics.
 * 
 * Learning Notes:
 * - Number formatting: Intl.NumberFormat for currency/percentage
 * - Conditional styling: Green for gains, red for losses
 * - Grid layout: Responsive card-based design
 */
export function SimulationResults({ results, investments }: SimulationResultsProps) {
  // Format currency
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value);
  };

  // Format percentage with commas for large values
  const formatPercent = (value: number) => {
    const sign = value >= 0 ? '+' : '';
    const formattedNumber = new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Math.abs(value));
    return `${sign}${formattedNumber}%`;
  };

  // Format shares with 4 decimal places (industry standard for fractional shares)
  const formatShares = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 4,
      maximumFractionDigits: 4,
    }).format(value);
  };

  // Animate metric values from 0 to target on render
  const animatedTotalInvested = useCountUp(results.totalInvested);
  const animatedCurrentValue = useCountUp(results.currentValue);
  const animatedAbsoluteGain = useCountUp(results.absoluteGain);
  const animatedPercentReturn = useCountUp(results.percentReturn);
  const animatedCagr = useCountUp(results.cagr);

  const isProfit = results.absoluteGain >= 0;

  return (
    <div className="simulation-results">
      {/* Key Metrics Cards */}
      <div className="simulation-results__metrics">
        <div className="metric-card">
          <div className="metric-card__label">Total Invested</div>
          <div className="metric-card__value">{formatCurrency(animatedTotalInvested)}</div>
        </div>

        <div className="metric-card">
          <div className="metric-card__label">Current Value</div>
          <div className="metric-card__value metric-card__value--highlight">
            {formatCurrency(animatedCurrentValue)}
          </div>
        </div>

        <div className={`metric-card metric-card--${isProfit ? 'gain' : 'loss'}`}>
          <div className="metric-card__label">Absolute Gain</div>
          <div className="metric-card__value">
            {formatCurrency(animatedAbsoluteGain)}
          </div>
        </div>

        <div className={`metric-card metric-card--${isProfit ? 'gain' : 'loss'}`}>
          <div className="metric-card__label">Percent Return</div>
          <div className="metric-card__value">
            {formatPercent(animatedPercentReturn)}
          </div>
        </div>

        <div className="metric-card metric-card--cagr">
          <div className="metric-card__label">CAGR</div>
          <div className="metric-card__value">
            {formatPercent(animatedCagr)}
          </div>
          <div className="metric-card__hint">Compound Annual Growth Rate</div>
        </div>
      </div>

      {/* Mobile sticky summary — visible only on small screens */}
      <div className="simulation-results__mobile-summary">
        <div className="mobile-summary__item">
          <span className="mobile-summary__label">Value</span>
          <span className="mobile-summary__value">{formatCurrency(results.currentValue)}</span>
        </div>
        <div className="mobile-summary__item">
          <span className={`mobile-summary__label ${isProfit ? 'text-green' : 'text-red'}`}>Return</span>
          <span className={`mobile-summary__value ${isProfit ? 'text-green' : 'text-red'}`}>
            {formatPercent(results.percentReturn)}
          </span>
        </div>
      </div>

      {/* Portfolio Header - Shows date range */}
      {investments && investments.length > 0 && (
        <PortfolioHeader
          holdings={results.holdings}
          investments={investments}
          endDate={results.timeline[results.timeline.length - 1]?.date || new Date().toISOString().split('T')[0]}
        />
      )}

      {/* Portfolio Chart */}
      <PortfolioChart 
        timeline={results.timeline} 
        totalInvested={results.totalInvested}
        investments={investments}
        showBenchmark={true}
        benchmarkTimeline={results.benchmarkTimeline}
        holdings={results.holdings}
        holdingsTimelines={results.holdingsTimelines}
      />

      {/* Holdings Table */}
      <div className="holdings-table">
        <h3 className="holdings-table__title">Holdings Breakdown</h3>
        
        <div className="holdings-table__container">
          <table className="holdings-table__table">
            <thead>
              <tr>
                <th>Asset</th>
                <th className="text-right">Invested</th>
                <th className="text-right">Shares</th>
                <th className="text-right">Purchase Price</th>
                <th className="text-right">Current Price</th>
                <th className="text-right">Current Value</th>
                <th className="text-right">Gain/Loss</th>
                <th className="text-right">Return %</th>
              </tr>
            </thead>
            <tbody>
              {results.holdings.map((holding) => {
                const isProfitable = holding.absoluteGain >= 0;
                return (
                  <tr key={holding.symbol}>
                    <td>
                      <div className="holdings-table__asset">
                        <span className="holdings-table__symbol">{holding.symbol}</span>
                        <span className="holdings-table__name">{holding.name}</span>
                      </div>
                    </td>
                    <td className="text-right" data-label="Invested">{formatCurrency(holding.invested)}</td>
                    <td className="text-right" data-label="Shares">{formatShares(holding.shares)}</td>
                    <td className="text-right" data-label="Purchase Price">{formatCurrency(holding.purchasePrice)}</td>
                    <td className="text-right" data-label="Current Price">{formatCurrency(holding.currentPrice)}</td>
                    <td className="text-right font-semibold" data-label="Current Value">{formatCurrency(holding.currentValue)}</td>
                    <td className={`text-right font-semibold ${isProfitable ? 'text-green' : 'text-red'}`} data-label="Gain/Loss">
                      {formatCurrency(holding.absoluteGain)}
                    </td>
                    <td className={`text-right font-semibold ${isProfitable ? 'text-green' : 'text-red'}`} data-label="Return %">
                      {formatPercent(holding.percentReturn)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Data Freshness Disclaimer */}
      <div className="data-disclaimer">
        <span className="disclaimer-icon">📅</span>
        <p className="disclaimer-text">
          Historical price data updated monthly on the 1st. 
          Current data as of: <strong>{new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}</strong>
        </p>
      </div>
    </div>
  );
}