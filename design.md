# Simple-Futoshiki Design Document

## 1. Overview
The Simple-Futoshiki application aims to provide a **clean, distraction-free, and modern** logic puzzle experience. The UI avoids clutter, focusing purely on usability and smooth interactions.

## 2. Technology Stack & UI Architecture
- **Framework**: Pure Jetpack Compose for the entirety of the UI.
- **Animations/Graphics**: Integration with **KorGE** (`KorGEView`) to render the animated characters (Ninja/Dragon).
- **Design Pattern**: Single Activity Architecture with state hoisted in a central `FutoshikiViewModel`. The app dynamically switches between distinct "Screens" (Landing, Game, Pause, Theming).

## 3. Themes & Color Palettes
The application is built around dynamic theming, heavily tied to the "Four Elements". The current theme dictates the primary accent color across the app.

### 3.1 The Elements (Accents)
- **Fire**: `#FF404E` (Red)
- **Water**: `#0088FF` (Blue)
- **Earth**: `#34C759` (Green)
- **Wood**: `#FF8D28` (Orange)

### 3.2 Light & Dark Modes
The app fully supports Light and Dark modes.
- **Light Mode Backgrounds**: Soft greys (`#F5F2F2` for background, `#F4F4F4` for surface).
- **Dark Mode Backgrounds**: Deep blacks/greys (`#0B0B0B` for background, `#161616` for surface).

## 4. Typography
The primary typeface used throughout the application is **Reem Kufi** (`reem_kufi_regular`, `medium`, `semibold`, `bold`). This provides a slightly stylized but highly legible typographic feel that fits the Japanese origins of the puzzle.

## 5. Core UI Components

### 5.1 Game Board & Cells (`PuzzleBoard`, `PuzzleCell`)
- The grid is dynamically sized based on the puzzle dimensions (e.g., 4x4, 5x5).
- Cells distinctively show states: locked (given numbers), selected, related (same row/column), and error (highlighted in red).
- Inequality constraints (`>`, `<`) are rendered in the slots between cells.

### 5.2 Wavy Underlines (`wavy` package)
The game uses highly stylized, animated wavy underlines that correspond to the chosen element:
- **Fire**: Slow rolling base with sharp flame tips.
- **Water**: Smooth, layered flowing waves.
- **Wood**: Organic canopy sway with drifting leaf particles.
- **Earth**: Slow strata layers shifting like soil sediment.

### 5.3 Modals & Overlays
- **Pause Overlay**: Uses an animated circular reveal originating from the Timer Pill. Dims the background and provides resume/quit options.
- **Win Modal**: Displayed upon solving a puzzle. Highlights the final time and prompts for a new game.

### 5.4 Theming Carousel (`ThemeCarousel`)
The theming screen incorporates a swipeable carousel allowing users to transition smoothly between Fire, Water, Earth, and Wood elements. It utilizes heavily blurred drop shadows and slide/fade transitions to give a premium feel.

## 6. Interaction & Flow
1. **Landing Screen**: Greets the user with a stylized logo, "Start", "Help", and "Themes" buttons. Transitions are animated smoothly via `AnimatedContent`.
2. **Theming Screen**: Accessed from the Landing or Pause menu to adjust the elemental theme or toggle Dark Mode.
3. **Game Screen**: The core experience. The top header hosts the title and timer, the middle contains the interactive grid, and the bottom contains the number pad and control pills.
4. **Pause State**: The timer pauses, and a full-screen overlay blocks the puzzle to prevent cheating while paused.

## 7. Refactoring Structure
To maintain scalability, the UI code is strictly broken down:
- `ui/screens/`
  - `game/` (`GameScreen`, `GameHeader`, `GameFooter`, `PuzzleBoard`, etc.)
  - `landing/` (`LandingScreen`, `LandingMenuContent`)
  - `theming/` (`ThemingScreen`, `ThemeCarousel`)
  - `pause/` (`PauseOverlay`)
- `ui/components/shared/` (Reusable components like `BigButton`, `TimerPill`, and the `wavy/` underlines)
