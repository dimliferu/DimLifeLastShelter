# DimLife: Last Shelter

A dark cyberpunk underground survival and bunker-management game for Android.

## Current prototype

Version **0.1.0** is the first playable foundation:
- Neon-green command-room interface.
- Power, water, oxygen and food supply indicators.
- Generator restart and water filtration actions.
- Advance a day and consume survival resources.
- GitHub Actions workflow to build a debug APK automatically.

## Build

Every push to `main` starts the **Android APK** workflow. Open the repository's **Actions** tab and select the latest successful run to download the `DimLife-Last-Shelter-debug` artifact.

The artifact is a debug APK for testing, not a production-signed Google Play release.

## Project details

- App name: **DimLife: Last Shelter**
- Application ID: `ru.dimlife.lastshelter`
- Language: Kotlin
- UI: Jetpack Compose
- Minimum Android version: Android 8.0 (API 26)

## Roadmap

- [x] Initial Android project and prototype screen
- [x] Basic survival resource loop
- [x] Automated debug APK workflow
- [ ] Persist game state between launches
- [ ] Room construction and upgrades
- [ ] Events, incidents and survivor needs
- [ ] Exploration and story progression
- [ ] Optional reward-based ads after core gameplay is stable
