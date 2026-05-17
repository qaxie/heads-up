# Architecture Spec — HeadsUp

## Overview

The app follows MVVM with a unidirectional data flow. There are four distinct layers:

```
┌─────────────────────────────────────────┐
│              UI Layer                   │  Jetpack Compose screens + ViewModels
├─────────────────────────────────────────┤
│           Repository Layer              │  Business logic, data access abstraction
├─────────────────────────────────────────┤
│            Data Layer                   │  DataStore (persistence)
│          + Package Layer                │  PackageManager (installed apps)
├─────────────────────────────────────────┤
│           Service Layer                 │  NotificationListenerService
└─────────────────────────────────────────┘
```

The service layer runs independently of the UI — it reads directly from the data layer and does not go through ViewModels.

---

## Layers

### UI Layer

Four screens, each backed by a ViewModel:

| Screen | ViewModel | Responsibility |
|---|---|---|
| `OnboardingScreen` | `OnboardingViewModel` | Check and react to notification access permission state |
| `HomeScreen` | `HomeViewModel` | Global toggle state, today's suppression count |
| `AppListScreen` | `AppListViewModel` | Load installed apps, read/write per-app allow state |

Navigation is linear: the app opens to `OnboardingScreen` if notification access is not granted, otherwise to `HomeScreen`. `AppListScreen` is reached from `HomeScreen`.

### Repository Layer

Two repositories, each exposed as an interface so they can be faked in tests:

**`AppPreferenceRepository`**
- `getAllPreferences(): Flow<List<AppPreference>>` — stream of all per-app settings
- `setAllowed(packageName: String, allowed: Boolean)` — update a single app's setting
- `isAllowed(packageName: String): Boolean` — point-in-time read used by the service

**`SuppressedCountRepository`**
- `getTodayCount(): Flow<Int>` — stream of today's suppression count
- `increment()` — called by the service each time a heads-up is suppressed
- `resetIfNewDay()` — called on service start and at midnight

### Data Layer

Persistence is handled by **Jetpack DataStore** (Preferences DataStore). No database is needed — the whitelist is a `Set<String>` of package names and the global toggle is a single `Boolean`.

Stored keys:
- `allowed_packages` — `Set<String>` of package names where heads-up is permitted
- `suppression_enabled` — `Boolean`, global on/off toggle
- `suppressed_count_date` — `String` (ISO date), the date the current count belongs to
- `suppressed_count_value` — `Int`, today's suppression count

### Package Layer

A single `InstalledAppSource` class wraps `PackageManager` and returns all installed apps split into two groups:
- **User apps** — `ApplicationInfo` where `FLAG_SYSTEM` is absent
- **System apps** — `ApplicationInfo` where `FLAG_SYSTEM` is present

Both groups are returned. The split is used by `AppListScreen` to render two separate sections. This is called once when `AppListScreen` loads and is not persisted.

### Service Layer

`HeadsUpListenerService` extends `NotificationListenerService`.

On `onNotificationPosted(sbn)`:
1. Check global toggle via `AppPreferenceRepository` — if disabled, return.
2. Check if the notification's effective importance is `IMPORTANCE_HIGH` — if not, return.
3. Check if the package is in the allowed set via `AppPreferenceRepository` — if allowed, return.
4. Call `adjustNotification()` with `KEY_IMPORTANCE = IMPORTANCE_DEFAULT` to dismiss the heads-up banner.
5. Call `SuppressedCountRepository.increment()`.

All packages — including system packages — go through this same logic. There is no blanket system exemption; the user's preference for each package governs behaviour.

The service reads from repositories directly (not through ViewModels). It holds no state of its own.

---

## Suppression Interface (ADR-001)

Per ADR-001, the suppression mechanism must be isolated so swapping from `NotificationListenerService` to `NotificationAssistantService` is a contained change.

The suppression logic is split into two concerns:

**Policy** (`SuppressionPolicy`) — answers "should this notification be suppressed?"
- Reads global toggle and per-app preference
- Shared between both service approaches

**Mechanism** — answers "how do we suppress it?"
- Current: `adjustNotification()` called inside `onNotificationPosted` in `HeadsUpListenerService`
- Fallback: `adjustEnqueuedNotification()` called inside `onNotificationEnqueued` in a future `HeadsUpAssistantService`

When the fallback is implemented, only the service class changes. `SuppressionPolicy`, both repositories, and all UI code remain untouched.

---

## Data Models

```
AppPreference
  packageName : String   // e.g. "com.whatsapp"
  isAllowed   : Boolean  // true = heads-up permitted

AppInfo
  packageName : String
  label       : String
  icon        : Drawable  // not persisted, loaded at runtime
  isSystem    : Boolean   // true = FLAG_SYSTEM is set
```

`AppInfo` is a UI model only — it is never written to DataStore. `AppPreference` is the persisted model. The `isSystem` flag is used solely for display grouping and the warning label; it does not affect suppression logic.

---

## Dependency Injection

Hilt is used for dependency injection. Repositories and the `InstalledAppSource` are provided as singletons. The `HeadsUpListenerService` receives its dependencies via `@Inject`.

---

## Permissions

| Permission | Declared in manifest | User grant required |
|---|---|---|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Yes (on the service) | Yes — via Settings > Notification Access |
| `QUERY_ALL_PACKAGES` | Yes | No |
| `POST_NOTIFICATIONS` (API 33+) | Yes | Yes — standard runtime prompt |

`POST_NOTIFICATIONS` is needed to post the persistent service status notification (optional feature, see `specs/01-product.md` US-04).

---

## Sequence: Notification Arrives

```
App posts notification
        │
        ▼
Android system determines importance
        │
        ▼
HeadsUpListenerService.onNotificationPosted()
        │
        ├── global toggle off? ──► return (no-op)
        │
        ├── importance < HIGH? ──► return (no-op)
        │
        ├── package in allowed set? ──► return (no-op)
        │    (applies to all packages, including system)
        │
        ▼
adjustNotification(KEY_IMPORTANCE = IMPORTANCE_DEFAULT)
        │
        ▼
SuppressedCountRepository.increment()
```

---

## References

- Suppression approach decision: `specs/decisions/ADR-001-notification-suppression-approach.md`
- User-facing behaviour and acceptance criteria: `specs/01-product.md`
