import { Outlet, Link, useLocation } from 'react-router-dom';
import { Home, LineChart, Info } from 'lucide-react';
import { ThemeToggle } from '../ThemeToggle';
import Logo from '../Logo';
import styles from './Layout.module.css';

export default function Layout() {
  const location = useLocation();
  
  const isActive = (path: string) => location.pathname === path;
  
  return (
    <div className={styles.layout}>
      <header className={styles.header}>
        <div className={styles.headerContainer}>
          <div className={styles.headerBrand}>
            <Logo size={32} />
            <h1 className={styles.headerLogo}>Moshimo</h1>
          </div>
          <nav className={styles.nav}>
            <Link 
              to="/" 
              className={`${styles.navLink} ${isActive('/') ? styles.navLinkActive : ''}`}
            >
              <Home size={20} strokeWidth={2} />
              <span>Home</span>
            </Link>
            <Link 
              to="/simulator" 
              className={`${styles.navLink} ${isActive('/simulator') ? styles.navLinkActive : ''}`}
            >
              <LineChart size={20} strokeWidth={2} />
              <span>Simulator</span>
            </Link>
            <Link 
              to="/about" 
              className={`${styles.navLink} ${isActive('/about') ? styles.navLinkActive : ''}`}
            >
              <Info size={20} strokeWidth={2} />
              <span>About</span>
            </Link>
          </nav>
          <div className={styles.headerActions}>
            <ThemeToggle />
          </div>
        </div>
      </header>
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
