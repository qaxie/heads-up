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
- [ ] `data_extraction_rules.xml` — define what data gets backed up or excluded before publishing to Play Store (Android Studio left a TODO placeholder here)
- [ ] `HeadsUpListenerService.kt:70` — broad `catch (_: Exception) {}` silently swallows reflection errors. Add logging or a fallback so failures are visible during development


- [ ] Remove `android.disallowKotlinSourceSets=false` from `gradle.properties` — find a proper fix or upgrade KSP to a version that natively supports AGP 9
- [ ] Enable minification (`isMinifyEnabled = true`) in release build type and add ProGuard rules
- [ ] Replace deprecated `LocalLifecycleOwner` in `OnboardingScreen.kt` with the `androidx.lifecycle.compose` alternative (warned at build time)
- [ ] Review all `@Suppress` annotations and either fix the underlying issue or document why suppression is justified
  - `InstalledAppSource.kt:15` — `@Suppress("DEPRECATION")` on `queryIntentActivities()`. The newer alternative requires API 33+, but minSdk is 24 so the old method is still needed for now

---

## Dependency Cleanup
- [ ] Fix inconsistent lifecycle library versions — `lifecycleRuntimeKtx` is on `2.6.1` but `lifecycleViewmodelCompose` is on `2.9.0`. Both should be on the same version
- [ ] Update outdated base library versions that were left at Android Studio defaults — `coreKtx = "1.10.1"` and `activityCompose = "1.8.0"` are significantly behind current releases
- [ ] Check if `datastore-preferences-core` still needs to be declared explicitly — it was added as a workaround and may now be pulled in transitively by `datastore-preferences`
- [ ] Fix misleading alias name in `libs.versions.toml` — `hilt-android-compiler` resolves to the `hilt-compiler` artifact, which is confusing for anyone updating dependencies

---

## Play Store Publishing
- [ ] Set up a release keystore and configure signing in `app/build.gradle.kts`
- [ ] Write a privacy policy (mandatory — app uses notification access which Google scrutinises)
- [ ] Prepare Play Store listing: screenshots, short description, full description
- [ ] Justify `QUERY_ALL_PACKAGES` permission in Play Store declaration (Google requires a written reason for this sensitive permission)
- [ ] Build a release AAB (`./gradlew bundleRelease`)
- [ ] Create a Google Play Console account and submit
