# BB Launcher

An Android 16 launcher that recreates the interaction model and visual character of BlackBerry 10 for personal use.

## Features

- Android Home role with Hub, Active Frames, and Apps pages
- BB10-inspired ribbon wallpaper, sharp panel chrome, and icon-only Flow navigation
- Searchable installed-app grid with functional Personal/Work profile tabs
- Persistent recent-app Active Frames plus live clock and battery cards
- In-launcher BB10-style status bar with time, network, battery, and unread count
- Notification-listener-backed Hub with grouped alerts and open, dismiss, and clear actions
- Pull-down notification shade with BB10-style Quick Settings
- Guided permission and HyperOS setup dashboard

The project uses original in-repository artwork and does not include extracted BlackBerry system assets.

## Build

Requirements:

- Android 11 or later
- Android Studio 2026.1.2 or a compatible command-line environment
- JDK 21
- Android SDK 36

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Install it on a connected device with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Device setup

Open **BB Setup** from the launcher shade to:

1. Select BB Launcher as the default Home app.
2. Enable notification access for the Hub.
3. Allow system-setting changes for the rotation tile.
4. On HyperOS, review Autostart and battery restrictions if Hub updates stop.

## Permissions

| Access | Purpose |
| --- | --- |
| Default Home role | Opens BB Launcher for Android Home navigation |
| Notification access | Reads and manages notifications shown in Hub |
| Modify system settings | Changes auto-rotate from Quick Settings |
| Network state | Displays the current connection type |

## Platform boundary

Stock Android does not allow a third-party launcher to replace the real status bar, notification shade, or Quick Settings outside the launcher. BB Launcher provides those surfaces while it is active and uses public Android APIs or user-controlled system panels for supported controls.
