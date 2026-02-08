import { Link } from 'react-router-dom';
import { TrendingUp, Briefcase, BarChart3 } from 'lucide-react';
import Logo from '../../components/Logo';
import './HomePage.css';

export default function HomePage() {
  return (
    <div className="home-page">
      <section className="hero">
        <div className="hero__logo-container">
          <Logo size={80} />
        </div>
        <h1 className="hero__title">Moshimo</h1>
        <p className="hero__subtitle">
          Investment "What If" Simulator
        </p>
        <p className="hero__description">
          What if you had invested in Apple in 2010? Tesla in 2015? <br />
          Find out with real historical market data.
        </p>
        <Link to="/simulator" className="hero__cta">
          Try Simulator →
        </Link>
      </section>
      
      <section className="features">
        <div className="feature">
          <div className="feature__icon"><TrendingUp size={48} strokeWidth={2} /></div>
          <h3 className="feature__title">Historical Simulation</h3>
          <p className="feature__description">
            See how investments would have performed over 20+ years of real market data
          </p>
        </div>
        <div className="feature">
          <div className="feature__icon"><Briefcase size={48} strokeWidth={2} /></div>
          <h3 className="feature__title">Multi-Asset Portfolios</h3>
          <p className="feature__description">
            Combine stocks, ETFs, and indexes to build diverse investment portfolios
          </p>
        </div>
        <div className="feature">
          <div className="feature__icon"><BarChart3 size={48} strokeWidth={2} /></div>
          <h3 className="feature__title">Interactive Charts</h3>
          <p className="feature__description">
            TradingView-powered visualization with multiple timeframe views
          </p>
        </div>
      </section>
      
      <section className="cta-section">
        <h2>Ready to explore?</h2>
        <p>Start simulating your investment strategies today</p>
        <Link to="/simulator" className="cta-button">
          Get Started
        </Link>
      </section>
    </div>
  );
}
