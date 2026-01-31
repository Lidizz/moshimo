import { PortfolioChart } from './PortfolioChart';
import { PortfolioHeader } from './PortfolioHeader';
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

  // Format percentage
  const formatPercent = (value: number) => {
    return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
  };

  // Format number with commas
  const formatNumber = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 8,
    }).format(value);
  };

  const isProfit = results.absoluteGain >= 0;

  return (
    <div className="simulation-results">
      {/* Key Metrics Cards */}
      <div className="simulation-results__metrics">
        <div className="metric-card">
          <div className="metric-card__label">Total Invested</div>
          <div className="metric-card__value">{formatCurrency(results.totalInvested)}</div>
        </div>

        <div className="metric-card">
          <div className="metric-card__label">Current Value</div>
          <div className="metric-card__value metric-card__value--highlight">
            {formatCurrency(results.currentValue)}
          </div>
        </div>

        <div className={`metric-card metric-card--${isProfit ? 'gain' : 'loss'}`}>
          <div className="metric-card__label">Absolute Gain</div>
          <div className="metric-card__value">
            {formatCurrency(results.absoluteGain)}
          </div>
        </div>

        <div className={`metric-card metric-card--${isProfit ? 'gain' : 'loss'}`}>
          <div className="metric-card__label">Percent Return</div>
          <div className="metric-card__value">
            {formatPercent(results.percentReturn)}
          </div>
        </div>

        <div className="metric-card metric-card--cagr">
          <div className="metric-card__label">CAGR</div>
          <div className="metric-card__value">
            {formatPercent(results.cagr)}
          </div>
          <div className="metric-card__hint">Compound Annual Growth Rate</div>
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
                <th>Stock</th>
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
                      <div className="holdings-table__stock">
                        <span className="holdings-table__symbol">{holding.symbol}</span>
                        <span className="holdings-table__name">{holding.name}</span>
                      </div>
                    </td>
                    <td className="text-right">{formatCurrency(holding.invested)}</td>
                    <td className="text-right">{formatNumber(holding.shares)}</td>
                    <td className="text-right">{formatCurrency(holding.purchasePrice)}</td>
                    <td className="text-right">{formatCurrency(holding.currentPrice)}</td>
                    <td className="text-right font-semibold">{formatCurrency(holding.currentValue)}</td>
                    <td className={`text-right font-semibold ${isProfitable ? 'text-green' : 'text-red'}`}>
                      {formatCurrency(holding.absoluteGain)}
                    </td>
                    <td className={`text-right font-semibold ${isProfitable ? 'text-green' : 'text-red'}`}>
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