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
- **Navigation**: `app/src/main/java/com/qaxie/headsup/ui/AppNavigation.kt` — checks notification access permission at startup to determine start destination (`onboarding` or `home`)
- **DI**: Hilt with KSP. `HeadsUpApplication` is the `@HiltAndroidApp` entry point. Use `@EntryPoint` + `EntryPointAccessors` for components that cannot use `@AndroidEntryPoint` (e.g. `NotificationListenerService`)
- **Persistence**: DataStore Preferences via `AppPreferenceRepositoryImpl` (stores suppressed packages, not allowed — default is ON for all apps) and `SuppressedCountRepositoryImpl` (today's suppression count with midnight reset)
- **Notification suppression**: `HeadsUpListenerService` intercepts notifications via `NotificationListenerService`. Suppression uses reflection to call `adjustNotification` with `android.service.notification.Adjustment` — this class is `@hide` in the public SDK stubs and cannot be imported directly

## Key Technology Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose (BOM 2026.02.01) + Material3 |
| DI | Hilt 2.59.2 + KSP |
| Navigation | Navigation Compose 2.9.0 |
| Persistence | DataStore Preferences 1.1.1 |
| Language | Kotlin 2.2.10 |
| Min SDK | 24 (Android 7.0) |
| Compile/Target SDK | 35 (Android 15) |
| Build | AGP 9.2.0, Gradle Kotlin DSL |

Dependencies are managed through a centralized version catalog at `gradle/libs.versions.toml`.

## Package Structure

```
com.qaxie.headsup
├── data/                        # Repositories and data models
│   ├── AppInfo.kt
│   ├── AppPreference.kt
│   ├── AppPreferenceRepository.kt (interface)
│   ├── AppPreferenceRepositoryImpl.kt
│   ├── InstalledAppSource.kt    # Queries PackageManager for launcher apps
│   ├── SuppressedCountRepository.kt (interface)
│   └── SuppressedCountRepositoryImpl.kt
├── di/
│   └── AppModule.kt             # Hilt bindings for DataStore and repositories
├── service/
│   ├── HeadsUpListenerService.kt
│   └── SuppressionPolicy.kt
└── ui/
    ├── AppNavigation.kt
    ├── applist/
    │   ├── AppListScreen.kt
    │   └── AppListViewModel.kt
    ├── home/
    │   ├── HomeScreen.kt
    │   └── HomeViewModel.kt
    ├── onboarding/
    │   ├── OnboardingScreen.kt
    │   └── OnboardingViewModel.kt
    └── theme/
```

## Known Gotchas

- **`android.service.notification.Adjustment` is `@hide`** — do not try to import it directly. Use reflection as implemented in `HeadsUpListenerService.suppressHeadsUp()`.
- **compileSdk must stay at 35** — `Adjustment` and `adjustNotification` are absent from the API 36 SDK stubs. Do not bump compileSdk to 36.
- **DataStore version must be `1.1.1`** — `1.1.5` does not exist and causes unresolved reference errors.
- **Hilt version must be `2.59.2`** — older versions are incompatible with AGP 9.x (`Android BaseExtension not found`).
- **KSP, not KAPT** — this project uses KSP for annotation processing. The Hilt compiler artifact is `com.google.dagger:hilt-compiler` (not `hilt-android-compiler`).
- **`android.disallowKotlinSourceSets=false`** in `gradle.properties` — required workaround for AGP 9 + KSP conflict.
- **App list uses launcher intent filter** — `InstalledAppSource` filters by `Intent.ACTION_MAIN + CATEGORY_LAUNCHER`, not by `FLAG_SYSTEM`. This correctly includes pre-installed apps like Gmail and YouTube while excluding unnamed system components.
- **Suppression is stored inverted** — `AppPreferenceRepositoryImpl` stores the set of SUPPRESSED packages. An app not in the set is considered allowed (default ON).

## Spec-Driven Development

All features are defined in `specs/` before implementation. Read the relevant spec before making changes to any screen or feature. Do not add behaviour that is not in the spec without discussing it first.

```
specs/
├── 01-product.md          # User stories and acceptance criteria
└── features/
    ├── 01-onboarding.md
    ├── 02-home.md
    └── 03-app-whitelist.md
```

## Testing

- **Unit tests** (`app/src/test/`): JUnit 4, run on JVM
- **Instrumented tests** (`app/src/androidTest/`): AndroidJUnit4 + Espresso + Compose UI Test, run on device/emulator
