# ReelWorld GO Mobile

This branch prepares a separately requested Google Play app using the ReelWorld Android WebView source.

- Display name: ReelWorld GO Mobile
- Application ID: `tech.illcoai.reelworldgomobile`
- Initial version: 1.0.0; version code 1
- Main branch retains the earlier Reel World Fishing identity.
- New Play app creation remains pending the owner's developer-policy and US export declarations.
- CI artifacts are explicitly unsigned and require secure signing before upload.
- Build with JDK 21, Gradle 9.3.1, SDK platform 36.1 and build tools 36.0.0.
- `gradle -PunsignedRelease=true :app:testDebugUnitTest :app:bundleRelease` runs existing unit checks and produces the unsigned bundle.
- Default release builds use KEYSTORE_PATH, STORE_PASSWORD and KEY_PASSWORD environment variables, with upload alias `upload`. Keep all keys and passwords outside this repository.

A successful build is not device testing or Play publication. Verify package, version, signature, install/startup and core flows; complete the new listing and all truthful declarations; then satisfy the new app's Console testing requirements before production.
