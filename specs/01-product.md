# Product Spec — HeadsUp

## Purpose

HeadsUp lets users control which apps are allowed to display heads-up (peek) notifications — the banners that slide down over active content. All other apps' notifications are silently delivered to the notification shade without interrupting the user.

---

## User Stories & Acceptance Criteria

### US-01 — Grant Notification Access

**As a user, I want to be guided through granting notification access so the app can start working.**

Acceptance criteria:
- AC-01.1: On first launch, if notification access is not granted, the app shows an onboarding screen explaining what the permission does and why it is needed.
- AC-01.2: The onboarding screen has a single call-to-action button that opens the system Notification Access settings page.
- AC-01.3: When the user returns to the app after granting access, the app automatically proceeds to the home screen without requiring any additional action.
- AC-01.4: If the user returns without granting access, the onboarding screen is shown again.
- AC-01.5: The app is non-functional (no suppression occurs) until notification access is granted.

---

### US-02 — Select Whitelisted Apps

**As a user, I want to select which apps are allowed to show heads-up notifications so I can control my interruptions.**

Acceptance criteria:
- AC-02.1: The app list screen shows all installed non-system apps with their icon and name.
- AC-02.2: Each app has a toggle. Toggle ON means heads-up notifications from that app are allowed. Toggle OFF means they are suppressed.
- AC-02.3: The default state for all apps is ON (allowed) on first install.
- AC-02.4: Toggle changes take effect immediately — the next notification from that app follows the new setting.
- AC-02.5: The user can search/filter the app list by app name.
- AC-02.6: The whitelist is persisted across app restarts and device reboots.

---

### US-03 — Suppress Heads-Up Notifications

**As a user, I want heads-up notifications from non-whitelisted apps to be suppressed so they don't interrupt me.**

The intended behaviour is identical to running `adb shell settings put global heads_up_notifications_enabled 0`, but applied selectively per app instead of globally. Only the visual heads-up banner is affected — every other aspect of the notification is left exactly as the originating app posted it.

Acceptance criteria:
- AC-03.1: When a notification with heads-up priority arrives from a non-whitelisted app, the heads-up banner is dismissed.
- AC-03.2: The notification still appears in the notification shade attributed to the original app — it is not cancelled, modified, or re-posted.
- AC-03.3: Sound, vibration, and LED alerts associated with the notification still fire normally and are not affected by suppression.
- AC-03.4: The notification's content, actions, and metadata are not altered in any way.
- AC-03.5: Notifications that are not heads-up level (e.g., low/medium priority) are not affected.
- AC-03.6: Notifications from whitelisted apps are never modified.
- AC-03.7: All apps with a launcher activity are shown in a single flat list regardless of whether they are pre-installed or user-installed. The FLAG_SYSTEM distinction is not exposed to the user — pre-installed apps that users interact with daily (e.g. Calendar, YouTube, Gmail) appear alongside sideloaded apps. Truly internal system components without a launcher activity are excluded entirely.

---

### US-04 — Enable / Disable Suppression Globally

**As a user, I want to pause all suppression with a single toggle so I can temporarily allow all heads-up notifications without changing my whitelist.**

Acceptance criteria:
- AC-04.1: The home screen has a prominent toggle to enable or disable the service globally.
- AC-04.2: When disabled, no notifications are modified regardless of whitelist state.
- AC-04.3: When re-enabled, the existing whitelist is restored immediately.
- AC-04.4: The global toggle state is persisted across app restarts.
- AC-04.5: The home screen clearly shows whether suppression is currently active or paused.

---

### US-05 — View Suppression Activity

**As a user, I want to see how many heads-up notifications have been suppressed so I can understand the app's impact.**

Acceptance criteria:
- AC-05.1: The home screen displays a count of heads-up notifications suppressed today.
- AC-05.2: The count resets at midnight each day.
- AC-05.3: The count is not shown (or shown as zero) when the service is disabled.

---

## Out of Scope

The following are explicitly not part of this version:

- **Per-channel control**: controlling heads-up at the notification channel level within a single app (e.g., allow WhatsApp messages but suppress WhatsApp calls).
- **Schedule-based rules**: time-of-day or calendar-based suppression rules.
- **Zero-flash guarantee**: the heads-up banner may briefly appear (~100–300ms) before suppression. Android provides no API for a non-system app to intercept a notification before it is rendered — `NotificationListenerService` is called after the system has already begun displaying the banner. Eliminating this flash entirely requires root or system-level app privileges, neither of which this app assumes.
- **Root-based suppression**: no root access is assumed or required.
- **Reliability of system notification suppression**: `adjustNotification()` may fail silently or behave inconsistently for system packages on some OEMs. The app exposes the control, but cannot guarantee suppression works for every system notification on every device.
