import { useEffect, useState } from 'react';
import { healthApi } from './services/api/healthApi';
import { stockApi } from './services/api/stockApi';
import { portfolioApi } from './services/api/portfolioApi';
import { InvestmentBuilder } from './components/InvestmentBuilder';
import { SimulationResults } from './components/SimulationResults';
import { TimeframeSelector } from './components/TimeframeSelector';
import { ThemeToggle } from './components/ThemeToggle';
import { LoadingSpinner } from './components/LoadingSpinner';
import { Toast } from './components/Toast';
import { PWAPrompt } from './components/PWAPrompt';
import Logo from './components/Logo';
import type { HealthResponse, Stock, SimulationRequest, SimulationResponse } from './types/api.types';
import './App.css';
import './components/Logo.css';

function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Simulation state
  const [isSimulating, setIsSimulating] = useState(false);
  const [simulationResults, setSimulationResults] = useState<SimulationResponse | null>(null);
  const [simulationError, setSimulationError] = useState<string | null>(null);
  
  // Timeframe state for chart sampling
  const [timeframe, setTimeframe] = useState<string>('ALL');
  
  // Store original request for re-simulation when timeframe changes
  const [lastRequest, setLastRequest] = useState<SimulationRequest | null>(null);
  
  // Toast notification
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Register service worker for PWA
  useEffect(() => {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker
        .register('/service-worker.js')
        .then((registration) => {
          console.log('Service Worker registered:', registration);
        })
        .catch((error) => {
          console.error('Service Worker registration failed:', error);
        });
    }
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);

        // Fetch health status and stocks in parallel
        const [healthData, stocksData] = await Promise.all([
          healthApi.checkHealth(),
          stockApi.getAllStocks(),
        ]);

        setHealth(healthData);
        setStocks(stocksData);

        console.log('Health:', healthData);
        console.log('Stocks:', stocksData);
      } catch (err: any) {
        console.error('Error fetching data:', err);
        setError(err.message || 'Failed to fetch data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleSimulate = async (request: SimulationRequest) => {
    try {
      setIsSimulating(true);
      setSimulationError(null);
      console.log('Starting simulation with request:', request);

      // Store request for timeframe changes
      setLastRequest(request);

      const results = await portfolioApi.simulate(request, timeframe);
      console.log('Simulation results:', results);

      setSimulationResults(results);
      
      // Show success toast
      setToast({ 
        message: '🎉 Simulation complete! Check out your results below.', 
        type: 'success' 
      });

      // Scroll to results
      setTimeout(() => {
        document.getElementById('results')?.scrollIntoView({ 
          behavior: 'smooth', 
          block: 'start' 
        });
      }, 100);

    } catch (err: any) {
      console.error('Simulation error:', err);
      setSimulationError(
        err.response?.data?.message || 
        err.message || 
        'Simulation failed. Please check your inputs and try again.'
      );
      setSimulationResults(null);
    } finally {
      setIsSimulating(false);
    }
  };

  /**
   * Handle timeframe changes - re-run simulation with new sampling.
   * This teaches students how the same data looks different at different scales.
   */
  const handleTimeframeChange = async (newTimeframe: string) => {
    setTimeframe(newTimeframe);
    
    // Re-run simulation with new timeframe if we have a previous request
    if (lastRequest) {
      try {
        setIsSimulating(true);
        setSimulationError(null);
        
        console.log('Re-running simulation with timeframe:', newTimeframe);
        
        const results = await portfolioApi.simulate(lastRequest, newTimeframe);
        setSimulationResults(results);
      } catch (err: any) {
        console.error('Timeframe change error:', err);
        setSimulationError(
          err.response?.data?.message || 
          err.message || 
          'Failed to update timeframe'
        );
      } finally {
        setIsSimulating(false);
      }
    }
  };

  if (loading) {
    return (
      <div className="app-loading">
        <div className="spinner"></div>
        <h1>Loading Moshimo...</h1>
        <p>Connecting to backend and fetching stock data</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="app-error">
        <h1>❌ Error</h1>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>Retry</button>
      </div>
    );
  }

  return (
    <div className="app">
      {/* PWA Install Prompt */}
      <PWAPrompt />

      {/* Toast notifications */}
      {toast && (
        <Toast 
          message={toast.message} 
          type={toast.type} 
          onClose={() => setToast(null)}
        />
      )}
      
      <header className="app-header">
        <div className="app-header__brand">
          <Logo size={40} />
          <div className="app-header__text">
            <h1 className="app-title">Moshimo</h1>
            <p className="app-subtitle">Investment Portfolio Simulator</p>
          </div>
        </div>
        <div className="app-header__theme-toggle">
          <ThemeToggle />
        </div>
      </header>

      <main className="app-main">
        {/* Investment Builder */}
        <InvestmentBuilder 
          stocks={stocks}
          onSimulate={handleSimulate}
          isSimulating={isSimulating}
        />

        {/* Simulation Error */}
        {simulationError && (
          <div className="simulation-error">
            <strong>Simulation Error:</strong> {simulationError}
          </div>
        )}

        {/* Loading Spinner */}
        {isSimulating && !simulationResults && (
          <LoadingSpinner message="Calculating portfolio performance..." />
        )}

        {/* Timeframe Selector - only show when we have results */}
        {simulationResults && (
          <TimeframeSelector
            selectedTimeframe={timeframe}
            onTimeframeChange={handleTimeframeChange}
            disabled={isSimulating}
          />
        )}

        {/* Results */}
        {simulationResults && lastRequest && (
          <div id="results">
            <SimulationResults 
              results={simulationResults} 
              investments={lastRequest.investments}
            />
          </div>
        )}

        {/* Debug Info (can be hidden in production) */}
        {health && (
          <details className="app-debug" style={{ marginTop: '3rem' }}>
            <summary>Backend Status</summary>
            <div style={{ background: 'var(--card-bg)', padding: '1rem', borderRadius: '8px', marginTop: '0.5rem', border: '1px solid var(--border-color)' }}>
              <p><strong>Status:</strong> {health.status}</p>
              <p><strong>Database:</strong> {health.database.connected ? '✅ Connected' : '❌ Disconnected'}</p>
              <p><strong>Version:</strong> {health.database.version}</p>
              <p><strong>Total Stocks:</strong> {health.database.totalStocks}</p>
              <p><strong>Total Prices:</strong> {health.database.totalPriceRecords}</p>
              <p><strong>Environment:</strong> {health.application.environment}</p>
            </div>
          </details>
        )}
      </main>
    </div>
  );
}

export default App;