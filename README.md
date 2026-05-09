# MediaControl

Android media controller — sends play/pause key events to the active media session on the system. **Not a player.** Works with Spotify, YouTube, system media, and any app that registers a media session.

## Features

- **iOS-style design** — circular buttons, dark/light surface, iOS system blue (#007AFF)
- **Two modes**
  - *Original* — separate Play and Pause buttons
  - *Compact* — single toggle button (dispatches PLAY/PAUSE based on state)
- **Full-screen (large) mode** — buttons fill the entire screen, system bars hidden
- **Dark / Light / System** theme support
- Screen stays on while app is open
- No ads, no network access, no media session of its own

## Screenshots

| Normal | Full-screen |
|--------|-------------|
| Two circular buttons, iOS aesthetic | Buttons stretch to fill display |

## Download

Grab the latest APK from [Releases](https://github.com/Ethan13322836698/MediaControl/releases/latest).

## How it works

```
AudioManager.dispatchMediaKeyEvent(KeyEvent(ACTION_DOWN, KEYCODE_MEDIA_PLAY))
AudioManager.dispatchMediaKeyEvent(KeyEvent(ACTION_UP,   KEYCODE_MEDIA_PLAY))
```

Targets whatever app currently holds the media session focus — same mechanism as headphone buttons and lock-screen controls.

## Build

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17+, Android SDK 34.

## Settings

Open via the gear icon (top-right corner):

| Option | Values |
|--------|--------|
| Mode | Original / Compact |
| Size | Normal / Full-screen |
| Theme | System / Light / Dark |

## Permissions

None. Uses only `AudioManager` which requires no declared permission.

## License

MIT
