# Feature Spec — Notification Suppression Service

**Satisfies**: US-03  
**Service**: `HeadsUpListenerService`  
**No UI — background behaviour only**

---

## Purpose

Intercepts incoming heads-up notifications from non-allowed apps and suppresses the heads-up banner while leaving all other notification behaviour unchanged.

---

## Service Lifecycle

`HeadsUpListenerService` is a bound system service managed entirely by Android. It is started automatically when notification listener access is granted and stopped automatically when access is revoked. The app does not start or stop it manually.

The service is stateless — it holds no in-memory cache of preferences. All reads are against `AppPreferenceRepository` and `DataStore` at the time each notification arrives.

---

## Suppression Logic

Executed on every call to `onNotificationPosted(sbn: StatusBarNotification)`:

```
1. If global suppression toggle is OFF               → return (no-op)
2. If sbn.notification.importance < IMPORTANCE_HIGH  → return (no-op)
3. If sbn.packageName is in the allowed set          → return (no-op)
4. Call adjustNotification() with KEY_IMPORTANCE = IMPORTANCE_DEFAULT
5. Call SuppressedCountRepository.increment()
```

All three no-op conditions are evaluated before any mutation occurs. There is no blanket exemption for system packages — all packages go through the same logic and the user's stored preference governs behaviour.

---

## No-Op Conditions in Detail

**Step 1 — Global toggle**  
Reads `suppression_enabled` from DataStore. If `false`, the service is passive and no notifications are touched regardless of whitelist state.

**Step 2 — Importance check**  
Only notifications with `IMPORTANCE_HIGH` produce a heads-up banner. Notifications at `IMPORTANCE_DEFAULT` or below are already non-intrusive and must not be modified. This check prevents the service from touching notifications it has already suppressed (re-entry guard) and notifications that were never heads-up candidates.

**Step 3 — Allowed set check**  
Reads `allowed_packages` from DataStore. If the notification's package name is present, the notification is left untouched. This applies equally to user apps and system packages — there is no special-casing by UID or package type.

---

## Suppression Mechanism

`adjustNotification()` is called with an `Adjustment` containing `KEY_IMPORTANCE = IMPORTANCE_DEFAULT`.

Effect:
- The heads-up banner is dismissed
- The notification remains in the shade attributed to the originating app
- Sound, vibration, and LED are not affected (they fired before `onNotificationPosted` was called)
- The notification's content, actions, and metadata are unchanged

This is consistent with the behaviour of `adb shell settings put global heads_up_notifications_enabled 0` applied selectively per app.

---

## Known Limitation

The heads-up banner may be briefly visible (~100–300ms) before `adjustNotification()` takes effect. This is an Android system constraint documented in ADR-001 and the product spec out-of-scope section. It is accepted behaviour for this implementation.

---

## Suppression Count

`SuppressedCountRepository.increment()` is called after every successful suppression (step 6). The repository handles the date boundary — if the stored date does not match today, the count is reset to zero before incrementing.

The count is only incremented when a suppression actually occurs (steps 1–4 all passed). No-ops do not affect the count.

---

## Threading

`onNotificationPosted` is called on the main thread. DataStore reads must be performed on a coroutine dispatcher (`Dispatchers.IO`). The service launches a coroutine for each notification event and does not block the main thread.

---

## What the Service Does Not Do

- It does not cancel notifications.
- It does not re-post notifications under the app's own identity.
- It does not modify notification content, actions, or metadata.
- It does not persist or log notification content.
- It does not interact with the UI layer.

---

## References

- Suppression approach and fallback: `specs/decisions/ADR-001-notification-suppression-approach.md`
- Accepted limitation (flash): `specs/01-product.md` — Out of Scope
