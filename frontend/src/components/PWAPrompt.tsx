/**
 * PWA Install Prompt Component
 * 
 * Displays a custom prompt to install the app as a PWA on mobile devices.
 * Shows only once per device (uses localStorage) and can be dismissed.
 */

import { useState, useEffect } from 'react';
import { Smartphone } from 'lucide-react';
import './PWAPrompt.css';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

const DISMISS_KEY = 'pwa-prompt-dismissed-at';
const DISMISS_DURATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

export function PWAPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [showPrompt, setShowPrompt] = useState(false);

  useEffect(() => {
    // Check if user has already installed the PWA
    const isInstalled = window.matchMedia('(display-mode: standalone)').matches;
    if (isInstalled) return;

    // Check if dismiss is still within the 7-day cooldown
    const dismissedAt = localStorage.getItem(DISMISS_KEY);
    if (dismissedAt) {
      const elapsed = Date.now() - Number(dismissedAt);
      if (elapsed < DISMISS_DURATION_MS) return;
      // Cooldown expired — remove stale key so we can show again
      localStorage.removeItem(DISMISS_KEY);
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
    const { outcome: _outcome } = await deferredPrompt.userChoice;

    // Clear the deferred prompt
    setDeferredPrompt(null);
    setShowPrompt(false);

    // Mark as seen (permanent for installs)
    localStorage.setItem(DISMISS_KEY, '0');
  };

  const handleDismiss = () => {
    setShowPrompt(false);
    // Store timestamp — prompt will reappear after DISMISS_DURATION_MS
    localStorage.setItem(DISMISS_KEY, String(Date.now()));
  };

  if (!showPrompt) return null;

  return (
    <div className="pwa-prompt">
      <div className="pwa-prompt__content">
        <div className="pwa-prompt__icon"><Smartphone size={32} strokeWidth={2} /></div>
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
