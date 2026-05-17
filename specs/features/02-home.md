# Feature Spec — Home

**Satisfies**: US-04, US-05  
**Screen**: `HomeScreen`  
**ViewModel**: `HomeViewModel`

---

## Purpose

The main screen of the app. Lets the user enable or disable suppression globally and shows how many heads-up notifications have been suppressed today.

---

## Entry Condition

Shown on launch when notification access is granted. Also the destination after `OnboardingScreen` transitions on permission grant.

---

## Screen States

### State 1 — Suppression Enabled

Displays:
- Status indicator showing suppression is **active**
- Global toggle in the ON position
- Today's suppression count: *"X heads-up notifications suppressed today"*
- Navigation entry point to the App List screen

### State 2 — Suppression Disabled

Displays:
- Status indicator showing suppression is **paused**
- Global toggle in the OFF position
- Count area is hidden or displays zero — the count is not meaningful when the service is not running

### State 3 — Notification Access Revoked (edge case)

If the user revokes notification access via system settings while the app is in the foreground, the ViewModel detects this on the next resume check and navigates to `OnboardingScreen`. `HomeScreen` is not shown in a degraded state.

---

## Behaviour

**Global toggle**
- Toggling OFF disables suppression immediately. The next notification from any app will not be intercepted, regardless of whitelist state.
- Toggling ON re-enables suppression immediately. The existing whitelist is restored — no reconfiguration required.
- State is persisted to DataStore. Restored correctly after app restart or device reboot.

**Suppression count**
- Reflects notifications suppressed since midnight of the current day.
- Updates in real time as the service suppresses notifications (via a `Flow` from `SuppressedCountRepository`).
- Resets to zero at midnight. The reset is triggered when the service processes its first notification after midnight, or when `HomeScreen` is opened after midnight.
- Displayed as a plain integer count, not a graph or history.

**Navigation to App List**
- A clearly labelled entry point (e.g. button or list item) navigates to `AppListScreen`.
- No data is loaded by `HomeViewModel` on behalf of `AppListScreen`.

---

## ViewModel Responsibilities

- Expose global toggle state as a `StateFlow<Boolean>`
- Expose today's suppression count as a `StateFlow<Int>`
- Handle toggle changes by writing to `AppPreferenceRepository`
- Check notification access permission on resume; navigate to `OnboardingScreen` if revoked

---

## Out of Scope for This Screen

- Suppression history beyond today's count.
- Per-app breakdown of suppression counts.
- Any configuration of suppression behaviour — that belongs in `AppListScreen` or a future settings screen.
