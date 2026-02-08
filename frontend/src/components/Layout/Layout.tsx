import { Outlet, Link, useLocation } from 'react-router-dom';
import { ThemeToggle } from '../ThemeToggle';
import Logo from '../Logo';
import './Layout.css';

export default function Layout() {
  const location = useLocation();
  
  const isActive = (path: string) => location.pathname === path;
  
  return (
    <div className="layout">
      <header className="header">
        <div className="header__container">
          <div className="header__brand">
            <Logo size={32} />
            <h1 className="header__logo">Moshimo</h1>
          </div>
          <nav className="nav">
            <Link 
              to="/" 
              className={`nav__link ${isActive('/') ? 'nav__link--active' : ''}`}
            >
              Home
            </Link>
            <Link 
              to="/simulator" 
              className={`nav__link ${isActive('/simulator') ? 'nav__link--active' : ''}`}
            >
              Simulator
            </Link>
            <Link 
              to="/about" 
              className={`nav__link ${isActive('/about') ? 'nav__link--active' : ''}`}
            >
              About
            </Link>
          </nav>
          <div className="header__actions">
            <ThemeToggle />
          </div>
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
