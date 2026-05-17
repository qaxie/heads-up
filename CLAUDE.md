# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew installDebug           # Build and install on connected device/emulator
./gradlew test                   # Run unit tests (JVM)
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew lint                   # Run lint checks
./gradlew clean                  # Clean build outputs
```

Run a single test class:
```bash
./gradlew test --tests "com.qaxie.headsup.ExampleUnitTest"
```

## Architecture & Structure

Single-module Android app (`app/`) using **Jetpack Compose** with **Material Design 3**.

- **Entry point**: `app/src/main/java/com/qaxie/headsup/MainActivity.kt` — sets up edge-to-edge display and hosts the root composable
- **Theme**: `app/src/main/java/com/qaxie/headsup/ui/theme/` — Material3 theming with dynamic color (Android 12+) and dark/light mode support

## Key Technology Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose (BOM 2026.02.01) + Material3 |
| Language | Kotlin 2.2.10 |
| Min SDK | 24 (Android 7.0) |
| Compile/Target SDK | 36 (Android 16) |
| Build | AGP 9.2.0, Gradle Kotlin DSL |

Dependencies are managed through a centralized version catalog at `gradle/libs.versions.toml`.

## Testing

- **Unit tests** (`app/src/test/`): JUnit 4, run on JVM
- **Instrumented tests** (`app/src/androidTest/`): AndroidJUnit4 + Espresso + Compose UI Test, run on device/emulator
