# Scann — QR & Barcode Scanner

Native Android app for scanning and generating QR codes / barcodes.
Package: `com.scann.thekami`

## Branch strategy

This repo uses a three-branch pipeline. Each branch maps to a specific stage of the release process, and each has its own GitHub Actions workflow.

| Branch | Purpose | What CI does | Touches Play Store? |
|---|---|---|---|
| `test` | Active development, experiments, WIP features/fixes | Lint + unit tests + builds a debug APK (uploaded as an artifact) | No |
| `main` | Stable, reviewed code — considered "ready to ship" | Builds a signed release AAB + APK, uploaded as artifacts for manual verification | No |
| `store` | Publish trigger | Builds a signed release AAB and pushes it to the **Open Testing** track on Google Play | Yes — Open Testing only |

### Workflow

1. **Develop on `test`.** Push freely — CI just lints, runs tests, and builds a debug APK so you can sanity-check on a device.
2. **Merge `test` → `main`** once a change is verified. This triggers a signed release build (AAB + APK) as a downloadable artifact. Nothing is published — this is a safety checkpoint to confirm the release build compiles and signs correctly before it goes anywhere near Play Store.
3. **Merge `main` → `store`** when you're ready to ship an update to testers. This automatically uploads the new AAB to Google Play's **Open Testing (beta)** track.
4. **Promoting to Production is always manual.** After verifying the Open Testing release in Play Console, use "Promote release" to push it to Production. This is intentionally not automated, to avoid an accidental bad release reaching all users.

### Versioning

`versionCode` and `versionName` are derived automatically from the git commit count (`app/build.gradle.kts`):
versionCode = total commit count on the branch
versionName = "1.0."

No manual version bumps needed — every commit to `main` or `store` produces a new, unique version automatically.

## Workflows (`.github/workflows/`)

- **`test.yml`** — runs on push to `test` and on PRs into `main`. Lint, unit tests, debug APK build.
- **`production-release.yml`** — runs on push to `main`. Signed AAB + APK build, uploaded as artifacts only.
- **`store-release.yml`** — runs on push to `store`. Signed AAB build, pushed to Play Store Open Testing track.

## Required secrets

Set these in the repo's GitHub Actions secrets:

- `KEYSTORE_BASE64` — base64-encoded release keystore
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `PLAY_STORE_SERVICE_ACCOUNT_JSON` — service account JSON for Play Developer API (used only by `store-release.yml`)

## Local setup

- JDK 17
- No Android Studio required for CI — this project is developed via Termux (Android) using a full terminal environment (git, gradlew, etc.)
- `./gradlew assembleDebug` — debug build
- `./gradlew bundleRelease` — signed release AAB (requires local `key.properties`, see `production-release.yml` for the expected format)
