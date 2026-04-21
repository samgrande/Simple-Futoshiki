# Futoshiki Puzzle Game

A clean and modern **Futoshiki** logic puzzle built entirely in **Kotlin + Jetpack Compose**, with Godot-powered animations.

---

## Repository

GitHub: [https://github.com/samgrande/Simple-Futoshiki](https://github.com/samgrande/Simple-Futoshiki)

---

## Features

- Minimal and modern UI design
- Challenging logic-based puzzles
- Smooth and responsive interactions
- Pure Kotlin + Jetpack Compose architecture
- Godot 4.6 animated characters (dragon, ninja)

---

## What is Futoshiki?

Futoshiki is a Japanese logic puzzle played on a square grid. The objective is to:

- Fill the grid with numbers
- Ensure no number repeats in any row or column
- Follow inequality constraints (`>`, `<`) between certain cells

---

## Building the App

The app has two components:
- **Simple-Futoshiki** — the Android Kotlin/Compose app (this repo)
- **Android-Godot-Futoshiki-library** — the Godot 4.6 animation project that produces `data.pck` (game content: dragon, ninja, scenes)

`godot-lib.aar` (bundled in `app/libs/`) is the Godot engine runtime. The animation content is `data.pck`, placed in `app/src/main/assets/`.

---

### Building Locally

**Prerequisites**

| Tool | Version | Notes |
|---|---|---|
| JDK | 21 (JBR) | Expected at `~/.sdkman/candidates/java/21.0.5-jbr` |
| Android SDK | latest | Expected at `~/android-sdk` |
| Godot | 4.6 stable headless | Must be on `PATH` or set `GODOT_PATH` |

---

**Step 1 — Export the animation PCK**

The Godot animation content must be exported first. It outputs `data.pck` directly into `app/src/main/assets/`.

```bash
cd ../Android-Godot-Futoshiki-library
./build.sh pck
```

This only needs to be re-run when the Godot animation project changes.

---

**Step 2 — Build the app**

```bash
cd Simple-Futoshiki

# Debug APK (for sideloading / testing)
./build.sh debug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release APK (unsigned without signing env vars)
./build.sh release
# Output: app/build/outputs/apk/release/app-release.apk

# Release AAB (for Play Store)
./build.sh aab
# Output: app/build/outputs/bundle/release/app-release.aab

# Full pipeline — exports Godot PCK then builds the AAB in one command
./build.sh pipeline
```

**Signing a release build** — set these before running `release` or `aab`:

```bash
export KEYSTORE_PATH=/path/to/your.jks
export KEYSTORE_PASSWORD=your_keystore_password
export KEY_ALIAS=your_key_alias
export KEY_PASSWORD=your_key_password
./build.sh release
```

---

### Building on GitHub Actions

Push to `main` — the workflow triggers automatically and:

1. Downloads the latest `data.pck` from the Godot library's GitHub Releases into `app/src/main/assets/`
2. Builds the debug APK, signed release APK, and release AAB
3. Attaches all three to a new GitHub Release tagged `build-N`

To trigger manually without pushing: **Actions → Build & Publish APKs → Run workflow**.

Downloaded artifacts are available under the release:
- `Futoshiki-debug.apk`
- `Futoshiki-release.apk`
- `Futoshiki-release.aab`

---

**Required secrets** (set once in Simple-Futoshiki → Settings → Secrets and variables → Actions)

| Secret | How to get it |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 release.jks` |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |
| `GH_PAT` | Personal Access Token with **Contents: Read** on `Android-Godot-Futoshiki-library` |

> **Creating `GH_PAT`**: GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens → create token with Contents: Read on the Godot library repo → copy and save as `GH_PAT` secret in this repo.

---

## Design Philosophy

- Keep it **minimal**
- Prioritize **usability**
- Maintain **clarity over complexity**
- Provide a **distraction-free puzzle experience**

---

## Future Improvements

- More puzzle sizes and difficulty levels
- Additional animation themes
- Leaderboard / time tracking
- Offline puzzle packs

---

## Contributing

Contributions are welcome!

1. Fork the repo
2. Create your feature branch
3. Commit your changes
4. Open a pull request

---

## License

This project is licensed under the **Apache License 2.0**.
