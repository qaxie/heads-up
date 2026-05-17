# TODO

## Testing
- [ ] Test in emulator — trigger a heads-up notification and verify suppression works (use SMS via Extended Controls in Android Studio)
- [ ] Test global toggle — disable suppression, confirm banners appear for all apps
- [ ] Test counter — verify suppressed count increments on home screen and resets at midnight
- [ ] Test search in app list
- [ ] Test on real phone

---

## Suppression Workaround
- [ ] Investigate a proper fix for the `Adjustment` reflection hack in `HeadsUpListenerService.suppressHeadsUp()` — `android.service.notification.Adjustment` is `@hide` in the public SDK stubs so it cannot be imported directly. Options to explore:
  - Try `adjustNotifications(List<Adjustment>)` (API 34+) which may be in the public stubs
  - Check if bumping compileSdk to 36 with a different AGP/Kotlin version resolves it
  - Research how other open-source notification apps handle this

---

## Code Cleanup
- [ ] Remove `android.disallowKotlinSourceSets=false` from `gradle.properties` — find a proper fix or upgrade KSP to a version that natively supports AGP 9
- [ ] Enable minification (`isMinifyEnabled = true`) in release build type and add ProGuard rules
- [ ] Replace deprecated `LocalLifecycleOwner` in `OnboardingScreen.kt` with the `androidx.lifecycle.compose` alternative (warned at build time)
- [ ] Review all `@Suppress` annotations and either fix the underlying issue or document why suppression is justified

---

## Play Store Publishing
- [ ] Set up a release keystore and configure signing in `app/build.gradle.kts`
- [ ] Write a privacy policy (mandatory — app uses notification access which Google scrutinises)
- [ ] Prepare Play Store listing: screenshots, short description, full description
- [ ] Justify `QUERY_ALL_PACKAGES` permission in Play Store declaration (Google requires a written reason for this sensitive permission)
- [ ] Build a release AAB (`./gradlew bundleRelease`)
- [ ] Create a Google Play Console account and submit
