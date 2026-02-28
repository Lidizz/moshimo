import { Link } from 'react-router-dom';
import { TrendingUp, Briefcase, BarChart3 } from 'lucide-react';
import Logo from '../../components/Logo';
import styles from './HomePage.module.css';

export default function HomePage() {
  return (
    <div className={styles.homePage}>
      <section className={styles.hero}>
        <div className={styles.heroLogoContainer}>
          <Logo size={80} />
        </div>
        <h1 className={styles.heroTitle}>Moshimo</h1>
        <p className={styles.heroSubtitle}>
          Investment "What If" Simulator
        </p>
        <p className={styles.heroDescription}>
          What if you had invested in Apple in 2010? Tesla in 2015? <br />
          Find out with real historical market data.
        </p>
        <Link to="/simulator" className={styles.heroCta}>
          Try Simulator →
        </Link>
      </section>
      
      <section className={styles.features}>
        <div className={styles.feature}>
          <div className={styles.featureIcon}><TrendingUp size={48} strokeWidth={2} /></div>
          <h3 className={styles.featureTitle}>Historical Simulation</h3>
          <p className={styles.featureDescription}>
            See how investments would have performed over 20+ years of real market data
          </p>
        </div>
        <div className={styles.feature}>
          <div className={styles.featureIcon}><Briefcase size={48} strokeWidth={2} /></div>
          <h3 className={styles.featureTitle}>Multi-Asset Portfolios</h3>
          <p className={styles.featureDescription}>
            Combine stocks, ETFs, and indexes to build diverse investment portfolios
          </p>
        </div>
        <div className={styles.feature}>
          <div className={styles.featureIcon}><BarChart3 size={48} strokeWidth={2} /></div>
          <h3 className={styles.featureTitle}>Interactive Charts</h3>
          <p className={styles.featureDescription}>
            TradingView-powered visualization with multiple timeframe views
          </p>
        </div>
      </section>
      
      <section className={styles.ctaSection}>
        <h2>Ready to explore?</h2>
        <p>Start simulating your investment strategies today</p>
        <Link to="/simulator" className={styles.ctaButton}>
          Get Started
        </Link>
      </section>
    </div>
  );
}
