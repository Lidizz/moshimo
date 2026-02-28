import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.svg'],
      manifest: {
        name: 'Moshimo - Investment Portfolio Simulator',
        short_name: 'Moshimo',
        description: 'Investment portfolio simulator and tracker for analyzing historical performance',
        start_url: '/',
        display: 'standalone',
        background_color: '#f0f4f0',
        theme_color: '#10b981',
        orientation: 'portrait-primary',
        scope: '/',
        categories: ['finance', 'business', 'productivity'],
        icons: [
          { src: 'pwa-64x64.png', sizes: '64x64', type: 'image/png' },
          { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' },
          { src: 'maskable-icon-512x512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
        screenshots: [
          { src: 'screenshots/desktop.png', sizes: '1280x720', type: 'image/png', form_factor: 'wide' as const },
          { src: 'screenshots/mobile.png', sizes: '390x844', type: 'image/png', form_factor: 'narrow' as const },
        ],
        shortcuts: [
          {
            name: 'New Simulation',
            short_name: 'Simulate',
            description: 'Start a new portfolio simulation',
            url: '/?action=new',
            icons: [{ src: 'pwa-192x192.png', sizes: '192x192' }],
          },
        ],
        prefer_related_applications: false,
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,woff2}'],
        navigateFallback: 'index.html',
        navigateFallbackDenylist: [/^\/api\//],
        runtimeCaching: [
          {
            urlPattern: /^\/api\//,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: { maxEntries: 50, maxAgeSeconds: 60 * 60 },
              cacheableResponse: { statuses: [0, 200] },
            },
          },
          {
            urlPattern: /^https:\/\/fonts\.(googleapis|gstatic)\.com\/.*/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'google-fonts',
              expiration: { maxEntries: 10, maxAgeSeconds: 60 * 60 * 24 * 365 },
              cacheableResponse: { statuses: [0, 200] },
            },
          },
        ],
      },
    }),
  ],
  css: {
    modules: {
      localsConvention: 'camelCase',
    },
  },
  server: {
    port: 5173,
    // Vite dev server handles SPA routing automatically
    // All non-file routes fall back to index.html
    proxy: {
      // Proxy API requests to Spring Boot backend
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  preview: {
    port: 5173,
    // Preview server also handles SPA routing
  }
})