# How to Generate PWA Icons

The logo component is currently an SVG. To create PWA icons, follow these steps:

## Option 1: Using Online Tools (Recommended)

### 1. Export Logo as High-Res PNG
```tsx
// Temporarily modify Logo.tsx to export at 512x512
<Logo size={512} />
```

1. Open the app in browser
2. Right-click the logo
3. "Inspect Element"
4. Right-click the SVG in DevTools
5. "Copy" > "Copy outerHTML"
6. Paste into https://www.svgviewer.dev/
7. Export as PNG at 512x512

### 2. Generate All Icon Sizes
Visit: https://realfavicongenerator.net/

1. Upload your 512x512 PNG
2. Configure settings:
   - **iOS**: Keep background (cream: #f5f1e8)
   - **Android**: Use adaptive icons
   - **Windows**: Tile color #5c9c9c
3. Download package
4. Extract to `frontend/public/icons/`

## Option 2: Using CLI Tool

```bash
npm install -g pwa-asset-generator

# From your logo SVG
pwa-asset-generator logo.svg ./public/icons \
  --background "#f5f1e8" \
  --icon-only \
  --padding "10%" \
  --manifest "./public/manifest.json"
```

## Required Icon Sizes

The manifest.json expects these sizes:
- 72x72
- 96x96
- 128x128
- 144x144
- 152x152
- 192x192
- 384x384
- 512x512

## Manual Creation (Photoshop/Figma)

1. Open logo in design tool
2. Set canvas to each size above
3. Center logo with 10% padding
4. Background: #f5f1e8 (cream) or transparent
5. Export as PNG
6. Name as: `icon-72x72.png`, `icon-96x96.png`, etc.
7. Place in `frontend/public/icons/`

## Verification

After generating icons:

1. Check files exist in `public/icons/`
2. Update manifest.json paths if needed
3. Build: `npm run build`
4. Preview: `npm run preview`
5. Open DevTools > Application > Manifest
6. Verify all icons load correctly
7. Test PWA installation

## Quick Test

Create a simple placeholder:
```bash
mkdir -p frontend/public/icons
# Use any square image as placeholder
cp path/to/square-image.png frontend/public/icons/icon-192x192.png
# Duplicate for other sizes
```

## Notes

- **iOS**: Prefers non-transparent backgrounds
- **Android**: Supports maskable icons (safe zone required)
- **Background Color**: Use cream (#f5f1e8) for consistency
- **Padding**: 10-20% safe zone around logo
- **Format**: PNG with transparency or solid background
- **Quality**: Use maximum quality for larger sizes

## Current Logo Colors

```css
/* The logo uses these CSS variables */
--accent: #5c9c9c (light mode)
--accent: #7eb3a9 (dark mode)
--accent-bg: rgba(92, 156, 156, 0.1)
```

For icons, use the light mode accent (#5c9c9c) with cream background (#f5f1e8) for consistency across all platforms.
