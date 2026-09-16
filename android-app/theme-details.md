# MIANUCOM Theme Details

This document outlines the design system and theme details used in the MIANUCOM application. The theme is built using Tailwind CSS and custom CSS properties.

## 🎨 Colors

The application uses a combination of primitive color ramps for decorative purposes and semantic tokens for component styling. Semantic tokens automatically switch between light and dark modes.

### Primitive Ramps
- **Primary**: A blue ramp ranging from `50` (`#F0F6FB`) to `950` (`#071D30`). Used primarily for gradients and decorative elements.
- **Gold**: A gold ramp ranging from `50` (`#FBF7EC`) to `900` (`#3D2F14`), with specific semantic mappings for accents (`rgb(var(--color-gold-accent))`).
- **Neutral**: Grayscale colors ranging from `0` (white) to `950` (`#171613`).

### Semantic Tokens (Light / Dark Mode)
Semantic tokens adapt based on the active theme (light or dark mode).

| Token | Light Mode (RGB) | Dark Mode (RGB) | Usage |
| :--- | :--- | :--- | :--- |
| **Surface** | `250 250 248` | `19 21 25` | Default page background |
| **Surface Raised** | `255 255 255` | `27 30 36` | Elevated elements (e.g., cards) |
| **Surface Sunken** | `244 244 241` | `14 16 19` | Inset backgrounds |
| **Surface Overlay** | `255 255 255` | `33 37 44` | Modals, dropdowns |
| **Surface Hover** | `247 247 244` | `35 39 46` | Hover states |
| **Border** | `231 230 225` | `42 47 55` | Standard borders |
| **Foreground (fg)** | `35 34 30` | `237 238 240` | Primary text |
| **Foreground Muted**| `93 92 85` | `135 141 150` | Secondary/Muted text |
| **Accent** | `33 104 155` | `90 155 203` | Primary brand color, links, buttons |
| **Success** | `46 125 79` | `87 180 127` | Success states |
| **Danger** | `179 66 62` | `224 133 129` | Error/Destructive states |
| **Warning** | `140 109 31` | `212 175 78` | Warning states |
| **Info** | `47 111 159` | `111 170 212` | Informational states |

---

## 🔤 Typography

- **Sans**: `Inter`, system-ui, sans-serif (Default body font)
- **Display**: `Sora`, Inter, system-ui, sans-serif (Used for headings and display text)
- **Serif**: `"Times New Roman"`, Georgia, serif (Used specifically for document styling, such as UN resolutions)

---

## 🌫️ Shadows & Effects

The application uses custom box shadows to create depth, particularly for glassmorphism and elevated components.

- **card**: Subtle shadow for standard cards.
- **raised**: Deeper shadow for elevated components.
- **overlay**: Deepest shadow for modals and popovers.
- **glass**: Complex shadow used in combination with backdrop-blur for glass panels.
- **glow**: An accent-colored glow effect used for active states or focus rings.

---

## 🎬 Animations & Keyframes

A rich set of micro-animations is provided to make the UI feel dynamic and responsive.

- **Fade & Scale**: `fade-in`, `fade-in-up`, `scale-in`, `slide-in-right` for mounting components.
- **Continuous**: 
  - `shimmer`: Loading skeleton effect.
  - `pulse-soft`: Gentle opacity pulsing.
  - `float`: Slow vertical translation (useful for badges or icons).
  - `gradient-shift`: Moving gradient backgrounds.
  - `sheen`: A sweeping reflection effect over glass surfaces.
  - `glow-pulse`: Pulsing opacity for glow elements.
  - `flash-danger`: A glowing red flash for critical warnings.

---

## 🧩 Custom CSS Components

The `index.css` file introduces specific component classes to ensure consistent styling for unique UI elements.

### Glassmorphism
Premium liquid-glass effects for surfaces:
- `.glass`: Standard glass surface with blur and border.
- `.glass-strong`: Heavier glass effect with increased blur and opacity.
- `.glass-sheen`: A specular top highlight that mimics light reflecting off a glass edge.

### UN Resolution Formatting
A suite of classes to format text according to UN document conventions:
- `.resolution-text`: Uses serif font, justified, leading-relaxed.
- `.preambulatory-clause`: Italicized, indented, prefixed with an em-dash.
- `.operative-clause`: Indented formatting for main actions.
- `.operative-verb`: Underlined and bolded verbs.
- `.sub-clause`: Deeply indented bullet points.
- `.document-header`: Centered, uppercase headers for official documents.

### Ambient Mesh
- `.app-ambient`: A fixed, ambient radial gradient mesh that sits behind the application shell to provide a rich, textured background.
