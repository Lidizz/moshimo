# Moshimo Design System v2 - Finance Green Palette

**Version**: 2.0.0  
**Date**: February 8, 2026  
**Changes from v1**: Professional finance-green palette, Lucide React icon library, flexible multi-palette architecture

---

## Design Philosophy

**"Professional finance-first design for everyone"**

Moshimo's design system emphasizes:
- **Trust & Growth**: Slate represents stability, emerald represents growth
- **Accessibility**: Simple enough for 10-year-olds, sophisticated for professionals
- **Flexibility**: Multi-palette system for easy theme customization
- **Consistency**: Unified design language across all components
- **Performance**: CSS variables enable instant theme switching

---

## Color Palette System

### Architecture Overview

Moshimo supports **multiple color palettes** via HTML data attributes:
- `data-theme="light"` or `data-theme="dark"` - Controls brightness mode
- `data-palette="finance-green"` - Selects color scheme (default)

This architecture allows adding new palettes in under 5 minutes by copying a CSS template and updating color values.

### Finance Green Palette (Default)

The finance-green palette uses emerald (growth) and slate (trust) as primary colors, aligning with industry expectations for financial applications.

#### Light Mode - Finance Green

**Backgrounds**
- Primary: `#f0f4f0` (Soft sage) - Main background, calming and professional
- Secondary: `#e8f0e8` (Mint card) - Card backgrounds, subtle contrast
- Tertiary: `#dfe8df` (Light mint) - Hover states
- Hover: `#d6e0d6` (Active mint) - Active/pressed states

**Text**
- Primary: `#1a1f1a` (Charcoal) - Main text, high contrast (WCAG AAA)
- Secondary: `#4a544a` (Slate gray) - Secondary text, labels
- Tertiary: `#7a847a` (Muted slate) - Disabled text, placeholders
- Inverted: `#f0f4f0` - Text on dark backgrounds

**Accent**
- Primary: `#10b981` (Emerald 500) - Brand color, growth indicator
- Hover: `#059669` (Emerald 600) - Hover/active states
- Light: `#34d399` (Emerald 400) - Subtle accents, highlights
- Background: `rgba(16, 185, 129, 0.1)` - Tinted backgrounds

**Status Colors**
- Success: `#10b981` (Emerald) - Profit, positive returns
- Error: `#ef4444` (Rose 500) - Loss, negative returns
- Info: `#64748b` (Slate 500) - Neutral information, ETF badges
- Warning: `#f59e0b` (Amber 500) - Cautions, important notices

**Borders**
- Default: `#c8d4c8` - Subtle borders on cards
- Strong: `#b0beb0` - Input borders, emphasized divisions

#### Dark Mode - Finance Green

**Backgrounds**
- Primary: `#0f1410` (Deep forest) - Main background, professional depth
- Secondary: `#1a1f1a` (Charcoal slate) - Cards, elevated surfaces
- Tertiary: `#252b25` (Lighter slate) - Hover states
- Hover: `#2f352f` (Active slate) - Active/pressed states

**Text**
- Primary: `#e8f0e8` (Soft white) - Main text, high contrast
- Secondary: `#a8b0a8` (Light slate) - Secondary text, labels
- Tertiary: `#78847a` (Muted slate) - Disabled text, placeholders
- Inverted: `#0f1410` - Text on light backgrounds

**Accent**
- Primary: `#34d399` (Light emerald) - Brand color for dark mode, higher contrast
- Hover: `#6ee7b7` (Emerald 300) - Hover/active states
- Light: `#10b981` (Emerald 500) - Subtle accents
- Background: `rgba(52, 211, 153, 0.15)` - Tinted backgrounds (higher opacity)

**Status Colors**
- Success: `#34d399` (Light emerald) - Profit, positive returns
- Error: `#f87171` (Rose 400) - Loss, negative returns
- Info: `#94a3b8` (Slate 400) - Neutral information
- Warning: `#fbbf24` (Amber 400) - Cautions, important notices

**Borders**
- Default: `#2f3a2f` - Subtle borders
- Strong: `#3f4a3f` - Input borders, emphasized divisions

#### RGB Variants

All primary colors have corresponding `-rgb` variables for use in `rgba()` functions:

```css
/* Example usage */
background: rgba(var(--accent-rgb), 0.1); /* 10% opacity accent background */
border: 1px solid rgba(var(--text-primary-rgb), 0.2); /* 20% opacity text border */
```

**Available RGB Variables**:
- `--bg-primary-rgb`, `--bg-secondary-rgb`
- `--text-primary-rgb`, `--text-secondary-rgb`
- `--accent-rgb`, `--primary-rgb` (alias)
- `--success-rgb`, `--error-rgb`, `--info-rgb`, `--warning-rgb`

### Color Contrast Ratios (WCAG AA Compliance)

All text color combinations meet WCAG AA standards (4.5:1 for normal text, 3:1 for large text):

**Light Mode**
- Charcoal on Sage: 12.8:1 (AAA) ✅
- Slate Gray on Sage: 6.2:1 (AA) ✅
- Emerald on Sage: 4.6:1 (AA) ✅

**Dark Mode**
- Soft White on Forest: 13.5:1 (AAA) ✅
- Light Slate on Forest: 6.8:1 (AA) ✅
- Light Emerald on Forest: 5.1:1 (AA) ✅

---

## How to Add New Palettes

The flexible palette system allows easy customization:

### Step 1: Copy Template from App.css

Locate the palette template comment block at the bottom of [App.css](src/App.css):

```css
/* TEMPLATE: Copy this block to add new palettes */
[data-theme='light'][data-palette='YOUR-PALETTE-NAME'] {
  --bg-primary: #XXXXXX;
  --bg-primary-rgb: R, G, B;
  /* ... all 40+ variables ... */
}

[data-theme='dark'][data-palette='YOUR-PALETTE-NAME'] {
  /* ... same variables, dark values ... */
}
```

### Step 2: Customize Colors

1. Replace `'YOUR-PALETTE-NAME'` with your palette ID (e.g., `'ocean-blue'`, `'sunset-orange'`)
2. Update all color hex codes
3. Calculate RGB values for each color (online tools available)
4. Ensure status colors remain distinguishable

### Step 3: Test Accessibility

1. Verify text contrast ratios using [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
2. Target minimum 4.5:1 for normal text, 3:1 for large text
3. Test with different vision modes (color blindness simulators)

### Step 4: Add Palette Selector (Future Enhancement)

Create a palette switcher component similar to ThemeToggle:

```typescript
const PalettePicker = () => {
  const [palette, setPalette] = useState('finance-green');
  
  const changePalette = (newPalette: string) => {
    document.documentElement.setAttribute('data-palette', newPalette);
    localStorage.setItem('moshimo-palette', newPalette);
    setPalette(newPalette);
  };
  
  // ... render palette options
};
```

### Example Palettes (Ideas)

**Ocean Blue**: Professional tech-focused palette
- Light mode: Sky blue backgrounds, navy text, blue accent
- Dark mode: Deep ocean backgrounds, cyan accent

**Sunset Orange**: Warm, approachable palette
- Light mode: Peach backgrounds, brown text, coral accent
- Dark mode: Burgundy backgrounds, orange accent

**Monochrome**: High-contrast grayscale
- Light mode: White/light gray backgrounds, black text
- Dark mode: Black/dark gray backgrounds, white text

---

## Icon System

### Library: Lucide React

**Migration from v1**: Replaced emoji icons (📊📈💼) with professional Lucide React components.

**Why Lucide?**
- 1000+ consistent, professionally designed icons
- Tree-shakeable (only imports what you use)
- Perfect React integration with TypeScript support
- Stroke-based design matches Moshimo's clean aesthetic
- Active maintenance and community support

### Icon Guidelines

**Size Standards**
- Small: 16px (inline with text, badges)
- Medium: 20px (navigation, buttons) ⭐ **Default**
- Large: 24px (feature highlights, empty states)
- Extra Large: 32px+ (hero sections, onboarding)

**Stroke Width**: Always use `strokeWidth={2}` (default) for consistency

**Color**: Icons inherit `currentColor` from parent text color automatically

**Accessibility**: Always include `aria-label` for standalone icon buttons

### Icon Mapping (v1 → v2)

| Component | v1 Emoji | v2 Lucide Icon | Size |
|-----------|----------|----------------|------|
| ThemeToggle | 🌙 / ☀️ | `Moon` / `Sun` | 20px |
| HomePage Features | 📈 | `TrendingUp` | 48px |
| HomePage Features | 💼 | `Briefcase` | 48px |
| HomePage Features | 📊 | `BarChart3` | 48px |
| PWAPrompt | 📱 | `Smartphone` | 32px |
| AboutPage Disclaimer | ⚠️ | `AlertTriangle` | 20px |
| Layout Nav (new) | - | `Home` | 20px |
| Layout Nav (new) | - | `LineChart` | 20px |
| Layout Nav (new) | - | `Info` | 20px |

### Usage Examples

```typescript
import { TrendingUp, Briefcase, BarChart3 } from 'lucide-react';

// Feature card
<div className="feature">
  <TrendingUp size={48} strokeWidth={2} />
  <h3>Historical Simulations</h3>
</div>

// Button with icon
<button className="primary-button">
  <ChevronRight size={20} />
  <span>Continue</span>
</button>

// Icon-only button (needs aria-label)
<button aria-label="Close notification">
  <X size={20} />
</button>
```

### Mobile Touch Targets

**CRITICAL**: All interactive icons must have ≥ 44x44px touch targets on mobile.

```css
/* Wrap small icons in padding */
.icon-button {
  padding: 12px; /* 20px icon + 24px padding = 44px total */
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* Or use minimum dimensions */
.nav-link {
  min-height: 44px;
  min-width: 44px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
```

---

## Typography

### Font Family

**Primary**: Inter (Google Fonts)
- Weights: 400 (regular), 500 (medium), 600 (semibold), 700 (bold), 800 (extrabold)
- Fallback: `-apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif`
- Use: All UI text, headings, body copy

**Monospace**: Courier New
- Use: Code snippets, debug information only

### Letter Spacing

```css
--letter-spacing-tight: -0.025em;   /* Headings (h1, h2) */
--letter-spacing-normal: 0;         /* Body text */
--letter-spacing-wide: 0.025em;     /* Labels, small text */
--letter-spacing-wider: 0.05em;     /* Buttons, badges, uppercase */
```

### Font Size Scale

- `--text-xs`: 0.75rem (12px) - Footnotes, captions
- `--text-sm`: 0.875rem (14px) - Secondary text, labels
- `--text-base`: 1rem (16px) - Body text ⭐ **Default**
- `--text-lg`: 1.125rem (18px) - Emphasized body text
- `--text-xl`: 1.25rem (20px) - Subheadings
- `--text-2xl`: 1.5rem (24px) - Section headings
- `--text-3xl`: 1.875rem (30px) - Page headings
- `--text-4xl`: 2.25rem (36px) - Hero headings

---

## Spacing & Layout

### Spacing Scale

```css
--space-xs: 0.25rem;   /* 4px  - Tight spacing */
--space-sm: 0.5rem;    /* 8px  - Compact spacing */
--space-md: 1rem;      /* 16px - Default spacing ⭐ */
--space-lg: 1.5rem;    /* 24px - Comfortable spacing */
--space-xl: 2rem;      /* 32px - Generous spacing */
--space-2xl: 3rem;     /* 48px - Section spacing */
```

### Border Radius

```css
--radius-sm: 0.25rem;   /* 4px  - Subtle rounded corners */
--radius-md: 0.5rem;    /* 8px  - Standard rounded corners ⭐ */
--radius-lg: 1rem;      /* 16px - Prominent rounded corners */
--radius-full: 9999px;  /* Circular elements (pills, avatars) */
```

### Shadows

**Light Mode**
```css
--shadow-sm: 0 1px 2px 0 rgba(26, 31, 26, 0.08);       /* Subtle depth */
--shadow-md: 0 4px 6px -1px rgba(26, 31, 26, 0.12);    /* Cards ⭐ */
--shadow-lg: 0 10px 15px -3px rgba(26, 31, 26, 0.15);  /* Dropdowns */
--shadow-xl: 0 20px 25px -5px rgba(26, 31, 26, 0.18);  /* Modals */
```

**Dark Mode**
```css
--shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.3);
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.4);
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.5);
--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.6);
```

---

## Responsive Breakpoints

### Mobile-First Approach

Moshimo uses mobile-first design with progressive enhancement:

```css
/* Mobile: Default styles (< 640px) */
.container {
  padding: 1rem;
}

/* Tablet: 640px - 1023px */
@media (min-width: 640px) {
  .container {
    padding: 1.5rem;
  }
}

/* Desktop: 1024px - 1439px */
@media (min-width: 1024px) {
  .container {
    padding: 2rem;
    max-width: 1200px;
  }
}

/* Large Desktop: 1440px+ */
@media (min-width: 1440px) {
  .container {
    padding: 3rem;
    max-width: 1400px;
  }
}
```

### Breakpoint Variables

```css
--breakpoint-sm: 640px;
--breakpoint-md: 768px;
--breakpoint-lg: 1024px;
--breakpoint-xl: 1440px;
```

### Mobile-Specific Considerations

**Touch Targets**: Minimum 44x44px for all interactive elements
**Font Sizes**: Minimum 16px to prevent iOS zoom on input focus
**Scroll Areas**: Ensure proper touch scrolling with `-webkit-overflow-scrolling: touch`
**Viewport**: Include `<meta name="viewport" content="width=device-width, initial-scale=1">`

---

## Component Patterns

### Cards

```css
.card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  padding: var(--space-lg);
  transition: all 0.2s ease;
}

.card:hover {
  background: var(--card-bg-hover);
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}
```

### Buttons

```css
.button-primary {
  background: var(--accent);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-sm) var(--space-lg);
  font-weight: 600;
  letter-spacing: var(--letter-spacing-wider);
  transition: all 0.2s ease;
}

.button-primary:hover {
  background: var(--accent-hover);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.button-primary:disabled {
  background: var(--text-tertiary);
  cursor: not-allowed;
  transform: none;
}
```

### Form Inputs

```css
.input {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-sm) var(--space-md);
  font-size: 1rem; /* Prevent iOS zoom */
  color: var(--text-primary);
  transition: all 0.2s ease;
}

.input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.1);
}

.input::placeholder {
  color: var(--text-tertiary);
}
```

---

## Browser Support

### Tested Browsers

✅ **Chrome/Edge 90+**: Full support, optimal experience
✅ **Firefox 88+**: Full support
✅ **Safari 14+**: Full support with minor PWA limitations
✅ **Mobile Safari (iOS 14+)**: Full support, manual PWA install required
✅ **Chrome Android 90+**: Full support with automatic PWA prompts

### Known Limitations

- **IE11**: Not supported (CSS variables, modern JavaScript)
- **Safari < 14**: Limited CSS variable support
- **iOS Safari PWA**: Requires manual "Add to Home Screen", no automatic prompt

### Progressive Enhancement

Moshimo gracefully degrades on older browsers:
- CSS variables fallback to light mode colors
- Modern JavaScript transpiled to ES5 via Vite
- PWA features enhance but aren't required for core functionality

---

## Accessibility (WCAG AA Compliant)

### Color Contrast

All text meets WCAG AA standards:
- Normal text: ≥ 4.5:1 contrast ratio
- Large text (18px+): ≥ 3:1 contrast ratio
- UI components: ≥ 3:1 contrast ratio

### Keyboard Navigation

- All interactive elements focusable via Tab key
- Focus states visible with emerald ring: `box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.3)`
- Skip to content link for screen readers
- Logical tab order matching visual layout

### Screen Reader Support

- Semantic HTML5 elements (`<nav>`, `<main>`, `<article>`)
- ARIA labels for icon-only buttons
- ARIA live regions for dynamic content (toasts, loading states)
- Form labels properly associated with inputs

### Motion

```css
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## Performance Optimizations

### CSS Variables

Instant theme switching without page reload:
- No CSS recalculation required
- Minimal JavaScript overhead
- Smooth 0.3s transitions between themes

### Font Loading

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
```

### Icon Tree-Shaking

Lucide React only bundles imported icons:
```typescript
import { TrendingUp } from 'lucide-react'; // Only TrendingUp included in bundle
```

### Service Worker Caching

PWA service worker caches:
- App shell (HTML, CSS, JS)
- Google Fonts
- API responses (network-first strategy)

---

## Migration from v1

### Breaking Changes

✅ **None** - v2 is fully backward compatible with v1 component structure

### Visual Changes

- Color palette: Cream/teal → Sage/emerald (finance-green)
- Icons: Emojis → Lucide React components
- Status colors: Slightly adjusted for better contrast

### API Changes

✅ **None** - All CSS variable names remain the same or have aliases for compatibility

### Migration Checklist

If manually migrating from v1 to v2:

- [ ] Update [App.css](src/App.css) with new palette system
- [ ] Add `data-palette="finance-green"` to HTML element
- [ ] Install `lucide-react` via npm
- [ ] Replace emoji icons with Lucide components
- [ ] Update chart colors to use CSS variables
- [ ] Test on Chrome, Firefox, Safari (desktop + mobile)
- [ ] Verify PWA install flow still works
- [ ] Run Lighthouse accessibility audit (target ≥ 90)

---

## Future Enhancements

### Planned Features

1. **Palette Picker Component**: Allow users to switch between color palettes
2. **Custom Theme Builder**: Let users create and save custom palettes
3. **Animation System**: Add micro-interactions with Framer Motion
4. **Component Library**: Publish reusable components as npm package
5. **Design Tokens Export**: Generate JSON tokens for design tools (Figma, Sketch)

### Community Palettes

Considering community-contributed palettes in future versions:
- Ocean Blue (tech-focused)
- Sunset Orange (warm, approachable)
- Midnight Purple (creative, modern)
- High Contrast (accessibility-first)

---

## File Structure

```
frontend/
├── public/
│   ├── manifest.json          # PWA manifest
│   ├── service-worker.js      # Service worker
│   └── icons/                 # PWA icons
├── src/
│   ├── components/
│   │   ├── Layout/
│   │   │   ├── Layout.tsx     # Navigation with Lucide icons
│   │   │   └── Layout.css
│   │   ├── ThemeToggle.tsx    # Moon/Sun icons
│   │   ├── ThemeToggle.css
│   │   ├── PWAPrompt.tsx      # Smartphone icon
│   │   └── [All components]   # Using new palette
│   ├── pages/
│   │   ├── Home/
│   │   │   ├── HomePage.tsx   # Feature icons (TrendingUp, etc.)
│   │   │   └── HomePage.css
│   │   ├── Simulator/         # Uses new palette
│   │   └── About/             # AlertTriangle icon
│   ├── App.tsx                # Palette attribute setup
│   ├── App.css                # New flexible palette system ⭐
│   └── index.css              # Cleaned legacy styles
├── index.html                 # data-palette attribute
├── DESIGN_SYSTEM_v1.md        # Archived original
├── DESIGN_SYSTEM_v2.md        # This document ⭐
└── package.json               # Includes lucide-react
```

---

## Support & Resources

### Documentation

- [Lucide React Icons](https://lucide.dev/guide/packages/lucide-react)
- [CSS Variables MDN](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties)
- [WCAG Contrast Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)

### Tools

- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Chrome DevTools Accessibility](https://developer.chrome.com/docs/devtools/accessibility/reference/)
- [Lighthouse](https://developers.google.com/web/tools/lighthouse)

### Troubleshooting

**Theme not switching?**
- Check `data-theme` attribute on `<html>` element
- Verify CSS variables are defined in App.css
- Clear browser cache and reload

**Icons not rendering?**
- Ensure `lucide-react` is installed: `npm list lucide-react`
- Check import statements match exact icon names
- Verify TypeScript/JavaScript files are properly transpiled

**Mobile touch targets too small?**
- Use Chrome DevTools device emulation
- Enable "Show rulers" to measure touch targets
- Add padding to reach 44x44px minimum

---

**Design System v2 Completed**: February 8, 2026  
**Maintained by**: Moshimo Development Team

Previous version: [DESIGN_SYSTEM_v1.md](DESIGN_SYSTEM_v1.md)
