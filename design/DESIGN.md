# Design System Specification: The Academic Curator

## 1. Overview & Creative North Star
The "Creative North Star" for this system is **The Academic Curator**. 

In an EdTech market saturated with neon "gamified" apps, we differentiate by leaning into a sophisticated, editorial aesthetic that evokes the feeling of a premium digital library or a high-end research journal. This system rejects the "template" look. We move away from rigid, boxed-in grids in favor of **Intentional Asymmetry** and **Tonal Depth**. 

The goal is to create a workspace that feels serious and intelligent—where a Korean professional feels their time is being respected. We achieve this through expansive white space, "ghost" boundaries, and a typography-first hierarchy that treats English and Korean text with equal architectural weight.

---

### 2. Colors & Surface Philosophy
The palette is rooted in a deep, authoritative Indigo, balanced by the refreshing clarity of Mint and the warmth of Amber.

#### The "No-Line" Rule
**Explicit Instruction:** Do not use 1px solid borders to section content. Boundaries must be defined solely through background color shifts or subtle tonal transitions. For example, a `surface-container-low` section sitting on a `surface` background provides all the definition a user needs without the visual "noise" of a stroke.

#### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of frosted glass or fine stationery.
- **Base Layer:** `surface` (#f8f9fa)
- **Secondary Content Area:** `surface-container-low` (#f3f4f5)
- **Primary Interactive Cards:** `surface-container-lowest` (#ffffff)
- **High-Intensity Callouts:** `primary-container` (#1a237e)

#### The "Glass & Gradient" Rule
To escape the "flat SaaS" look, use Glassmorphism for floating navigation bars or modal overlays. 
- **Effect:** Apply `surface-container-lowest` at 80% opacity with a `20px` backdrop-blur.
- **Signature Textures:** For primary CTAs and Progress Headers, use a subtle linear gradient: `primary` (#000666) to `primary-container` (#1a237e) at a 135-degree angle. This adds a "soul" and depth that flat hex codes cannot achieve.

---

### 3. Typography
The system utilizes a dual-font strategy to ensure the "Academic Curator" vibe remains consistent across languages.

*   **Display & Headlines:** **Manrope.** A geometric sans-serif with a high x-height that feels modern yet authoritative.
*   **Body & UI:** **Inter** (English) and **Pretendard** (Korean). These are functionally identical in weight and tracking, ensuring a seamless visual flow when English and Korean appear in the same sentence.

#### Hierarchy Scale
- **Display-LG (3.5rem):** Reserved for hero milestones or large-scale data points.
- **Headline-MD (1.75rem):** Used for primary section headers. Lean into `on-surface` (#191c1d) with a font-weight of 700.
- **Title-SM (1rem):** The workhorse for card titles. 
- **Label-MD (0.75rem):** Used for "Metadata" (e.g., word count, difficulty level). Use `on-surface-variant` (#454652).

---

### 4. Elevation & Depth
We eschew traditional structural lines for **Tonal Layering**.

*   **The Layering Principle:** Place a `surface-container-lowest` card (Pure White) on a `surface-container-low` background. The slight shift in gray-scale creates a soft, natural lift.
*   **Ambient Shadows:** When a card must float (e.g., a vocabulary popover), use an extra-diffused shadow: `box-shadow: 0 12px 32px rgba(25, 28, 29, 0.04)`. The shadow color is a tinted version of `on-surface`, never pure black.
*   **The "Ghost Border" Fallback:** If a border is required for accessibility in input fields, use `outline-variant` (#c6c5d4) at 20% opacity. **Forbid 100% opaque borders.**
*   **Interactive Radius:** 
    *   **LG (1rem / 16px):** Standard for main content cards.
    *   **Full (9999px):** Reserved for Pill-shaped buttons and search bars.

---

### 5. Components

#### Buttons & Action Items
*   **Primary:** Gradient fill (`primary` to `primary-container`). Rounded-full. No border. Text: `on-primary`.
*   **Secondary (AI Accents):** Use `secondary-container` (#8bf1e6). This "Mint" highlight signifies AI-powered features or progress-related actions.
*   **Tertiary (Audio/Review):** Use `tertiary-fixed-dim` (#ffba38) for "Save to Review" or "Favorite" actions to provide a warm, motivating focal point.

#### Cards & Progress Bars
*   **Cards:** Never use dividers. Use `Spacing-6` (1.5rem) to separate internal content.
*   **Progress Bars:** Background: `surface-container-high`. Fill: `secondary` (#006a63). The bar should be thin (4px) with rounded ends to maintain an elegant, non-intrusive feel.

#### Input Fields
*   **State:** Soft-wash background (`surface-container-lowest`).
*   **Focus:** A 2px "Ghost Border" using `primary` at 30% opacity. No "glow" effects.

#### Specialized EdTech Components
*   **Transcript Player:** Use a `surface-container-low` background for the container, and highlight the "active" English phrase by elevating it onto a `surface-container-lowest` card.
*   **Review Chips:** Use `tertiary-container` (#422c00) with `on-tertiary-fixed` (#281900) text for a high-contrast, premium "Golden" feel for saved vocabulary.

---

### 6. Do’s and Don’ts

#### Do
*   **Do** use asymmetrical margins (e.g., more padding on the left than the right in hero sections) to create an editorial, magazine-like layout.
*   **Do** prioritize vertical whitespace over lines. When in doubt, increase the spacing from `Spacing-4` to `Spacing-8`.
*   **Do** use `backdrop-blur` for all navigation elements to keep the user grounded in their current context.

#### Don't
*   **Don't** use pure black (#000000) for text. Always use `on-surface` (#191c1d) to maintain the premium, "ink-on-paper" feel.
*   **Don't** use high-saturation reds for errors. Use the `error` token (#ba1a1a) which is tuned to be legible but sophisticated.
*   **Don't** use standard 1px dividers between list items. Use a `0.5px` offset in background color or simply more breathing room.