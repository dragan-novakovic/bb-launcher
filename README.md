# BB Launcher

An Android 16 launcher that recreates the interaction model and visual character of BlackBerry 10 for personal use.

## Current status

The project foundation contains a dark, edge-to-edge Jetpack Compose application targeting API 36. Launcher, application grid, Active Frames, Hub, and in-launcher SystemUI features will be added incrementally.

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
