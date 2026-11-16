# ReelWorld GO Android release

This repository is the Android WebView client for https://reelworld.illcoai.tech.

## Existing Google Play app

- Existing app record: Reel World Fishing.
- Application ID: `tech.illcoai.reelworldgo`. Preserve this identity.
- Next version: 1.0.1, version code 2.
- Minimum SDK: 24; target SDK: 36; compile SDK: 36.1.
- Version 1 is available to internal testers. Production is inactive.

## Build

Use JDK 21 with a Java compiler and Gradle 9.3.1, plus Android SDK platform 36.1 and build tools 36.0.0.

The Android release bundle workflow runs existing unit tests and builds an **unsigned** AAB. Its artifact must be signed with the registered upload key before Play upload. An unsigned artifact is not installable or ready for publication.

For a locally signed release, set `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` in a secure process environment; the upload alias is `upload`. Then run `gradle :app:bundleRelease`. Keep all signing files and passwords outside the repository.

## Release gates

1. Verify successful compilation, existing unit tests, and actual Android installation/startup and core gameplay.
2. Recover the registered upload key, or finish Google's upload-key reset before submitting a newly signed bundle.
3. Verify the final bundle package, version, signature, and checksum.
4. Complete Play listing visuals, app access, ads, content rating, audience, data safety, and financial declarations using verified facts.
5. Upload to the existing app and verify the track status.
6. Complete the Console-required closed test before applying for production. As observed November 16, 2026, the account requires 12 opted-in testers for 14 days; no testers were opted in.

Free download pricing was saved November 16, 2026. This does not complete store setup or imply public availability.
