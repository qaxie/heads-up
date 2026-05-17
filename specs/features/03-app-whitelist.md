# Feature Spec — App Whitelist

**Satisfies**: US-02  
**Screen**: `AppListScreen`  
**ViewModel**: `AppListViewModel`

---

## Purpose

Lets the user control which apps are allowed to show heads-up notifications. Apps toggled ON are allowed; apps toggled OFF are suppressed.

---

## Entry Condition

Reached by navigating from `HomeScreen`. Notification access is guaranteed to be granted at this point.

---

## Screen States

### State 1 — Loading

Shown briefly while `PackageManager` is queried for installed apps and DataStore is read for saved preferences.

Displays a loading indicator. No app list is shown.

### State 2 — Populated (default)

Displays a single scrollable list of all apps that have a launcher activity, sorted alphabetically by name. Pre-installed apps (e.g. Calendar, YouTube, Gmail) and user-installed apps are shown together — the `FLAG_SYSTEM` distinction is not exposed to the user. Truly internal system components without a launcher activity are excluded.

Each entry shows:
- App icon
- App name
- Toggle (ON = heads-up allowed, OFF = suppressed)

Default toggle state on first install: **ON** (allowed) for all apps.

### State 3 — Search Active

User has entered text in the search field.

Displays a filtered subset of the full list matching the search query. Matching is case-insensitive against the app name. The toggle on each result behaves identically to State 2.

If no apps match the query, displays a plain empty state message: *"No apps match your search."*

### State 4 — Empty (edge case)

No non-system apps are installed. Unlikely in practice.

Displays a plain empty state message: *"No apps found."* No search bar is shown.

---

## Behaviour

**Loading**
- App list and preferences are loaded in parallel on screen entry.
- If loading takes more than 300ms, the loading indicator is shown. Below that threshold, the transition to the populated state is direct with no visible flash.

**Toggle interaction**
- Toggling an app writes the change to `AppPreferenceRepository` immediately.
- The change takes effect for the next notification from that app — the service reads preferences at notification time, not on a cache.
- There is no confirm dialog. Toggles are reversible and low-risk.

**Search**
- The search field is always visible at the top of the screen in States 2 and 3.
- Filtering is applied locally on the already-loaded list — no re-query of `PackageManager`.
- Clearing the search field returns to State 2.

**List currency**
- The app list is loaded once on screen entry. Apps installed or uninstalled while this screen is open are not reflected until the user leaves and returns.
- If an app is uninstalled that had a saved preference, the stale preference entry in DataStore is harmless — the service will never receive a notification from a package that is not installed.

**System package suppression reliability**
- `adjustNotification()` may fail silently or behave inconsistently for system packages on some OEMs. The toggle is always shown, but suppression is best-effort for system packages.

**Back navigation**
- Standard back press returns to `HomeScreen`. No save action is required — all changes are written immediately on toggle.

---

## ViewModel Responsibilities

- Query `PackageManager` for all apps with a launcher activity via `InstalledAppSource`
- Merge app list with saved preferences from `AppPreferenceRepository` into a single `StateFlow<AppListState>`
- Expose search query as a `MutableStateFlow<String>` and apply filtering reactively
- Write preference changes to `AppPreferenceRepository` on toggle

---

## Data Model (UI only)

```
AppListItem
  packageName : String
  label       : String
  icon        : Drawable
  isAllowed   : Boolean

AppListState
  userApps    : List<AppListItem>   // all apps, no system split
```

---

## Out of Scope for This Screen

- Per-channel control within a single app.
- Sorting or grouping options beyond alphabetical by name.
- Bulk actions (e.g. "allow all" / "suppress all").
