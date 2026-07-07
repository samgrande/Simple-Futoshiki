<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="128" alt="Futoshiki Logo"/>
</p>

<h1 align="center">Simple Futoshiki</h1>

<p align="center">
  <a href="https://f-droid.org/en/packages/com.hexcorp.futoshiki/">
    <img src="https://img.shields.io/f-droid/v/com.hexcorp.futoshiki?style=flat-square&logo=fdroid&label=F-Droid" alt="F-Droid"/>
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/license-Apache%202.0-blue?style=flat-square" alt="License"/>
  </a>
  <a href="https://kotlinlang.org/">
    <img src="https://img.shields.io/badge/Kotlin-2.2.10-purple?style=flat-square&logo=kotlin" alt="Kotlin"/>
  </a>
  <a href="https://github.com/samgrande/Simple-Futoshiki/releases">
    <img src="https://img.shields.io/github/v/release/samgrande/Simple-Futoshiki?style=flat-square&logo=github" alt="GitHub Release"/>
  </a>
</p>

<p align="center">
  A clean and modern <strong>Futoshiki</strong> logic puzzle game built with <strong>Kotlin + Jetpack Compose</strong> and <strong>KorGE</strong>-powered animations.
</p>

<p align="center">
  <a href="https://f-droid.org/en/packages/com.hexcorp.futoshiki/">
    <img src="https://img.shields.io/badge/Get%20it%20on-F--Droid-0880C4?style=for-the-badge&logo=fdroid" alt="Get it on F-Droid"/>
  </a>
</p>

---

## What is Futoshiki?

Futoshiki is a Japanese logic puzzle played on a square grid. The objective is to fill the grid with numbers so that no digit repeats in any row or column, while satisfying the "greater than" (`>`) / "less than" (`<`) inequality constraints between adjacent cells.

---

## Screenshots

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200" alt="Gameplay"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200" alt="Puzzle solving"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200" alt="Theme selection"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200" alt="Game screen"/>
</p>

---

## Features

- **Three grid sizes** (4×4, 5×5, 6×6) with Easy, Medium, and Hard difficulty
- **Material 3 interface** built entirely with Jetpack Compose
- **Unique-solution generator** with backtracking verification
- **Animated ninja vs dragon chase** powered by KorGE
- **Custom theming** with 4 color themes and multiple theme modes
- **Pause & resume** with timer and ranking system
- **No ads, no tracking, no network permissions** — only `VIBRATE` for haptic feedback

---

## Building

```bash
# Debug APK (for local testing)
./build.sh debug

# Release APK (unsigned — F-Droid signs with its own key)
./build.sh release

# Release AAB (for Play Store)
./build.sh aab
```

See [DEV-Notes.md](DEV-Notes.md) for detailed build instructions, keystore setup, and the release workflow.

---

## License

```
Copyright 2026 HeX

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
