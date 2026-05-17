# Feature Spec — Onboarding

**Satisfies**: US-01  
**Screen**: `OnboardingScreen`  
**ViewModel**: `OnboardingViewModel`

---

## Purpose

Gate access to the app until the user grants notification listener access. This permission is mandatory — the app cannot function without it.

---

## Entry Condition

`OnboardingScreen` is shown on app launch when `NotificationManagerCompat.getEnabledListenerPackages()` does not include the app's package name.

If permission is already granted, the app navigates directly to `HomeScreen` and `OnboardingScreen` is never shown.

---

## Screen States

### State 1 — Permission Not Granted (initial)

Displays:
- App name and brief description of what the app does
- A plain-language explanation of why notification access is needed:  
  *"HeadsUp needs to read incoming notifications in order to selectively suppress heads-up banners. It does not read notification content for any other purpose."*
- A single primary button: **"Grant Access"**

### State 2 — Returned Without Granting

User tapped "Grant Access", was taken to system settings, and returned without granting.

Displays the same content as State 1. No error message or retry counter — just the same screen. The button remains **"Grant Access"**.

### State 3 — Permission Granted (transitional)

Detected when the app resumes (`onResume`) and the permission check now passes.

Navigates immediately to `HomeScreen`. No success message or confirmation screen.

---

## Behaviour

- **"Grant Access" button**: opens the system Notification Access settings page via  
  `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.
- **Permission check timing**: checked on every `onResume`. This covers the case where the user grants access in system settings and returns to the app.
- **Back navigation**: if the user presses back on `OnboardingScreen`, the app exits. There is no partial state to preserve.
- **Re-entry**: if the user later revokes notification access via system settings while the app is running, the app must return to `OnboardingScreen` on the next resume. The service stops automatically when access is revoked — no additional cleanup is needed.

---

## Out of Scope for This Screen

- Explaining how to use the app beyond what is needed to motivate granting the permission.
- Any secondary permissions (e.g. `POST_NOTIFICATIONS`) — these are requested at the point of use, not during onboarding.
