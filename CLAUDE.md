# Simple Futoshiki — AI Coding Guide

Kotlin + Jetpack Compose Android puzzle game. Single-activity, MVVM, enum-based navigation.

---

## Project Layout

```
app/src/main/java/com/hexcorp/futoshiki/
├── MainActivity.kt                  # Entry point, FutoshikiApp(), AnimatedContent nav
├── game/
│   ├── FutoshikiViewModel.kt        # All state: GameState, timer, theme persistence
│   └── PuzzleEngine.kt              # Puzzle generation, constraint logic, win check
└── ui/
    ├── theme/Theme.kt               # AppTheme, ThemeMode, FutoshikiColors, ReemKufi font
    ├── screens/
    │   ├── game/                    # GameScreen, PuzzleBoard, PuzzleCell, NumberPad, WinModal
    │   ├── landing/LandingScreen.kt
    │   ├── pause/PauseOverlay.kt
    │   └── theming/                 # ThemingScreen, ThemeModeSlider
    └── components/shared/           # BigButton, TimerPill, DraggableSizeTabs, HelpPanel, etc.

app/src/main/res/
├── drawable/                        # SVG vectors: theme logos (fire/water/earth/wood), futo_logo, shuriken
├── drawable-night/                  # Dark-mode variants (ic_launcher_foreground)
├── values/ic_launcher_background.xml
├── values-night/ic_launcher_background.xml
├── mipmap-anydpi-v26/               # Adaptive icon XMLs
└── font/                            # reem_kufi_*.ttf (Regular/Medium/SemiBold/Bold)
```

---

## Architecture Rules

- **Navigation**: `Screen` enum (`LANDING, GAME, PAUSE, THEMING`) — no Navigation Compose. Add screens by extending this enum and adding an `AnimatedContent` branch in `MainActivity.kt`.
- **State**: Single `GameState` data class flowing through `StateFlow`. All mutations go through `FutoshikiViewModel`. Never hold screen-level state in the ViewModel — use Compose `remember` for that.
- **Theme**: Read via `LocalAppTheme.current` and `LocalIsDark.current`. Colors come from `FutoshikiColors` — always call the `@Composable` helper (`FutoshikiColors.background()`) not the raw constant. Accent color: `accentColor()`.
- **Font**: Always use `fontFamily = ReemKufi`. No other typefaces in the UI.
- **Persistence**: SharedPreferences key names: `app_theme`, `theme_mode`, `game_size`, `is_dark`.

---

## Common Import Pitfalls

These have caused build failures before — always double-check:

| Symbol | Correct import |
|---|---|
| `blur()` modifier | `androidx.compose.ui.draw.blur` |
| `ColorFilter` | `androidx.compose.ui.graphics.ColorFilter` |
| `Offset` (geometry) | `androidx.compose.ui.geometry.Offset` — never import twice |
| `rotate()` modifier | `androidx.compose.ui.draw.rotate` — never import twice |
| `clip()` modifier | `androidx.compose.ui.draw.clip` |

Wildcard imports (`import androidx.compose.foundation.*`) can create ambiguous `Offset` conflicts — prefer explicit imports in new files.

---

## Build

```bash
# Debug APK → app/build/outputs/apk/debug/app-debug.apk
./build.sh debug

# Release APK (unsigned without env vars)
./build.sh release

# Or directly with Gradle
./gradlew assembleDebug
./gradlew assembleRelease
```

**Prerequisites**: `JAVA_HOME` → JDK 21 (JBR), `ANDROID_HOME` → Android SDK.  
**Release signing env vars**: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.  
CI/CD runs on push to `main` via `.github/workflows/build.yml` — produces GitHub Release artifacts automatically.

**No Firebase**: The project had Firebase removed. Do not re-add `com.google.gms.google-services` plugin or Firebase dependencies without a real `google-services.json`.

---

## Skills

### /build
Build a debug APK and report success or errors.
```
Run: ./build.sh debug
Report the build output. If it fails, identify the error, fix it, and re-run.
```

### /new-screen <ScreenName>
Add a new screen to the app following project conventions.
```
Steps:
1. Add `<ScreenName>` to the Screen enum in FutoshikiViewModel.kt
2. Create app/src/main/java/com/hexcorp/futoshiki/ui/screens/<screenname>/<ScreenName>Screen.kt
   - Accept state + callback lambdas as params (no ViewModel reference inside Composable)
   - Use BackHandler for back navigation
   - Background: Modifier.background(FutoshikiColors.background())
   - Wrap content in systemBarsPadding() + widthIn(max = 420.dp)
   - Font: fontFamily = ReemKufi
3. Add an AnimatedContent branch in MainActivity.kt → FutoshikiApp()
   - Use the same slide+fade transitionSpec as existing screens
4. Add navigation methods in FutoshikiViewModel.kt if needed
5. Run ./build.sh debug to verify
```

### /new-component <ComponentName>
Add a new shared Composable component.
```
Steps:
1. Create app/src/main/java/com/hexcorp/futoshiki/ui/components/shared/<ComponentName>.kt
2. Use explicit imports (no wildcards) to avoid Offset/rotate ambiguity
3. Read theme via LocalIsDark.current and LocalAppTheme.current
4. Colors: use FutoshikiColors.* @Composable helpers, never raw constants
5. Run ./build.sh debug to verify
```

### /add-theme <ThemeName>
Add a new accent theme (like FIRE, WATER, EARTH, WOOD).
```
Steps:
1. Add `<ThemeName>` to AppTheme enum in ui/theme/Theme.kt
2. Add `<ThemeName>Accent` Color constant to FutoshikiColors object
3. Add case to the `accentColor()` when-expression in Theme.kt
4. Add case to `FutoshikiTheme()` themeResId when-expression
5. Add `<Style.Theme.Futoshiki.<ThemeName>` in res/values/themes.xml with a primary color
6. Add a `when` branch for the new theme in ThemingScreen.kt canvas dot drawing
7. Add a logo drawable: res/drawable/<themename>.xml (SVG vector, 324×297 viewport)
8. Add the theme to the `themes` list in ThemingScreen.kt
9. Run ./build.sh debug to verify
```

### /add-dark-icon
Add or update the dark-mode launcher icon.
```
Files involved:
- res/drawable/ic_launcher_foreground.xml        → light mode icon foreground
- res/drawable-night/ic_launcher_foreground.xml  → dark mode icon foreground (white strokes)
- res/values/ic_launcher_background.xml          → light bg color (#FCFAF5)
- res/values-night/ic_launcher_background.xml    → dark bg color (#1A1A1D)
- res/mipmap-anydpi-v26/ic_launcher.xml          → references the above via @color and @drawable

The night qualifiers are picked up automatically by launchers that support adaptive icons.
Grid strokes: #F5F2F2 (dark mode), #000000 (light mode).
Kanji/detail fills: #F0EFEA (dark mode), original dark values (light mode).
Constraint arrows: #D4282C (red, both modes).
```

### /check-imports <file>
Audit a file for the known import issues that break this project's build.
```
Read the specified file and check:
1. Is `blur` used? → needs `import androidx.compose.ui.draw.blur`
2. Is `ColorFilter` used? → needs `import androidx.compose.ui.graphics.ColorFilter`
3. Is `Offset` imported more than once? → remove the duplicate
4. Is `rotate` imported more than once? → remove the duplicate
5. Report any issues found and fix them.
```

### /fix-build
Diagnose and fix the current build failure.
```
1. Run ./build.sh debug and capture output
2. For each error line:
   - "Unresolved reference 'X'" → find the correct import, add it to the file
   - "Conflicting import" → find and remove the duplicate import
   - "File google-services.json is missing" → remove id("com.google.gms.google-services") from app/build.gradle.kts plugins block and remove firebase-bom + firebase-analytics from dependencies
   - Other Kotlin errors → read the file at the reported line, understand the issue, fix it
3. Re-run ./build.sh debug until it succeeds
```
