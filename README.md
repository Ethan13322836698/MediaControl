# MediaControl

**English** | [中文](#中文)

---

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

---

# 中文

**[English](#mediacontrol)** | 中文

---

Android 媒体控制工具 — 向系统当前活跃的媒体会话发送播放/暂停按键事件。**本身不是播放器。** 支持 Spotify、YouTube、系统媒体及任何注册了媒体会话的应用。

## 功能

- **iOS 风格设计** — 圆形按钮、深色/浅色背景、iOS 系统蓝 (#007AFF)
- **两种模式**
  - *原始模式* — 独立的播放和暂停按钮
  - *简洁模式* — 单个切换按钮（根据状态发送 PLAY/PAUSE）
- **特大模式（全屏）** — 按钮铺满整个屏幕，隐藏系统栏
- **深色 / 浅色 / 跟随系统** 主题支持
- 使用期间保持亮屏
- 无广告、无网络访问、不占用媒体会话

## 下载

从 [Releases](https://github.com/Ethan13322836698/MediaControl/releases/latest) 下载最新 APK。

## 工作原理

```
AudioManager.dispatchMediaKeyEvent(KeyEvent(ACTION_DOWN, KEYCODE_MEDIA_PLAY))
AudioManager.dispatchMediaKeyEvent(KeyEvent(ACTION_UP,   KEYCODE_MEDIA_PLAY))
```

控制当前持有媒体会话焦点的应用 — 与耳机按钮、锁屏控件机制相同。

## 编译

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

需要 JDK 17+、Android SDK 34。

## 设置项

点击右上角齿轮图标打开：

| 选项 | 可选值 |
|------|--------|
| 模式 | 原始 / 简洁 |
| 大小 | 普通 / 全屏 |
| 主题 | 跟随系统 / 浅色 / 深色 |

## 权限

无。仅使用 `AudioManager`，无需声明任何权限。

## 许可证

MIT
