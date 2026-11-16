# ReelWorld GO Mobile

This branch prepares a separately requested Google Play app using the ReelWorld Android WebView source.

- Display name: ReelWorld GO Mobile
- Application ID: `tech.illcoai.reelworldgomobile`
- Initial version: 1.0.0; version code 1
- Main branch retains the earlier Reel World Fishing identity.
- New Play app created after owner confirmation of developer-policy and US export declarations.
- Version 1.0.0 / code 1 was uploaded and published to internal testing on November 16, 2026. Console confirmed Active and Available to internal testers.
- Existing 11-person ReelWorld tester list assigned. Selection is not evidence of opt-in or installation.
- Signed local build succeeded; all 8 unit tests and release lint passed; bundle structure, package, version and signature verified.
- SHA-256: a0b5e4e19c6a9925a2f4865d0b0dc2a88b8190db29ab504976655a169e3e07dc.
- Physical device testing has not been performed. Public production release remains pending listing, declarations, required closed testing, and production access.
- CI artifacts are explicitly unsigned and require secure signing before upload.
- Build with JDK 21, Gradle 9.3.1, SDK platform 36.1 and build tools 36.0.0.
- `gradle -PunsignedRelease=true :app:testDebugUnitTest :app:bundleRelease` runs existing unit checks and produces the unsigned bundle.
- Default release builds use KEYSTORE_PATH, STORE_PASSWORD and KEY_PASSWORD environment variables, with upload alias `upload`. Keep all keys and passwords outside this repository.

A successful build is not device testing or Play publication. Verify package, version, signature, install/startup and core flows; complete the new listing and all truthful declarations; then satisfy the new app's Console testing requirements before production.
