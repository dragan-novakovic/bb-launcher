# BB Launcher

An Android 16 launcher that recreates the interaction model and visual character of BlackBerry 10 for personal use.

## Current status

The project contains a dark, edge-to-edge Jetpack Compose application targeting API 36. It is registered as an Android Home application and provides the initial Hub and Active Frames shell plus a searchable grid of installed applications.

## Build

Requirements:

- Android Studio 2026.1.2 or a compatible command-line environment
- JDK 21
- Android SDK 36

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Platform boundary

Stock Android does not allow a third-party launcher to replace the real status bar, notification shade, or Quick Settings outside the launcher. BB Launcher will provide those surfaces while it is active and use public Android APIs for notification access and supported controls.
