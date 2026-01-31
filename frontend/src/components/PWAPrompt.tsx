/**
 * PWA Install Prompt Component
 * 
 * Displays a custom prompt to install the app as a PWA on mobile devices.
 * Shows only once per device (uses localStorage) and can be dismissed.
 */

import { useState, useEffect } from 'react';
import './PWAPrompt.css';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export function PWAPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [showPrompt, setShowPrompt] = useState(false);

  useEffect(() => {
    // Check if user has already dismissed or installed
    const hasSeenPrompt = localStorage.getItem('pwa-prompt-dismissed');
    const isInstalled = window.matchMedia('(display-mode: standalone)').matches;

    if (hasSeenPrompt || isInstalled) {
      return;
    }

    // Listen for the beforeinstallprompt event
    const handler = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e as BeforeInstallPromptEvent);
      
      // Show prompt after 30 seconds of usage
      setTimeout(() => {
        setShowPrompt(true);
      }, 30000);
    };

    window.addEventListener('beforeinstallprompt', handler);

    return () => {
      window.removeEventListener('beforeinstallprompt', handler);
    };
  }, []);

  const handleInstall = async () => {
    if (!deferredPrompt) return;

    // Show the install prompt
    deferredPrompt.prompt();

    // Wait for the user to respond
    const { outcome } = await deferredPrompt.userChoice;

    console.log(`User ${outcome} the install prompt`);

    // Clear the deferred prompt
    setDeferredPrompt(null);
    setShowPrompt(false);

    // Mark as seen
    localStorage.setItem('pwa-prompt-dismissed', 'true');
  };

  const handleDismiss = () => {
    setShowPrompt(false);
    localStorage.setItem('pwa-prompt-dismissed', 'true');
    
    // Allow showing again after 7 days
    setTimeout(() => {
      localStorage.removeItem('pwa-prompt-dismissed');
    }, 7 * 24 * 60 * 60 * 1000); // 7 days
  };

  if (!showPrompt) return null;

  return (
    <div className="pwa-prompt">
      <div className="pwa-prompt__content">
        <div className="pwa-prompt__icon">📱</div>
        <div className="pwa-prompt__text">
          <h3 className="pwa-prompt__title">Install Moshimo</h3>
          <p className="pwa-prompt__description">
            Get instant access to your portfolio simulator with our app. 
            Works offline and feels like a native app!
          </p>
        </div>
        <div className="pwa-prompt__actions">
          <button
            onClick={handleInstall}
            className="pwa-prompt__button pwa-prompt__button--primary"
          >
            Install
          </button>
          <button
            onClick={handleDismiss}
            className="pwa-prompt__button pwa-prompt__button--secondary"
          >
            Not Now
          </button>
        </div>
      </div>
    </div>
  );
}
