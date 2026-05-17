# ADR-001 — Notification Suppression Approach

**Status**: Accepted  
**Date**: 2026-05-03

---

## Context

The app needs to suppress the heads-up (peek) banner for notifications from selected apps while leaving everything else — sound, vibration, LED, notification shade entry, original app attribution — completely unchanged.

Two Android APIs can achieve this. A decision is needed on which to use first and under what condition we would switch.

---

## Decision

Use `NotificationListenerService` as the initial implementation.

---

## Rationale

`NotificationListenerService`:
- Any number of listener services can run simultaneously on a device. There is no conflict with other apps or system services.
- Supported from API 18. Compatible with the project's current `minSdk` of 24 without change.
- Well-documented, widely used by third-party apps, and battle-tested across OEMs.

`NotificationAssistantService` is not chosen at this stage because:
- Only one notification assistant can be active on a device at a time. Selecting HeadsUp as the assistant would displace any existing assistant (e.g. Google's on Pixel, Samsung's on Galaxy), removing features the user may depend on.
- Requires API 29 (Android 10), which would require raising `minSdk` from 24 to 29.

---

## Known Limitation of the Chosen Approach

`onNotificationPosted` fires after the system has already begun rendering the heads-up banner. Suppression via `adjustNotification()` will dismiss the banner, but a brief visual flash (~100–300ms) may be visible before dismissal. This is an Android system constraint and cannot be eliminated without root or system-level privileges.

---

## Fallback: NotificationAssistantService

If the flash from `NotificationListenerService` proves unacceptable in practice, the identified upgrade path is `NotificationAssistantService`:

- `onNotificationEnqueued()` fires **before** the notification is posted, eliminating the flash entirely.
- `adjustEnqueuedNotification()` with `KEY_IMPORTANCE` lowered from `IMPORTANCE_HIGH` to `IMPORTANCE_DEFAULT` suppresses the heads-up banner while preserving sound and vibration.
- **Tradeoff accepted when switching**: the user must designate HeadsUp as their sole notification assistant in system settings, displacing any existing assistant. `minSdk` must also be raised to 29.

---

## Consequences

- The architecture must be designed so the suppression logic is isolated behind an interface, making a future swap from `NotificationListenerService` to `NotificationAssistantService` a contained change.
- The brief flash is documented as a known limitation in the product spec (see `specs/01-product.md`, Out of Scope).
