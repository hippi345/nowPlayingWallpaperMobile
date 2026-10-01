# Android — Spotify now-playing wallpaper

## Use Joel's existing Spotify app

Do **not** create a new Spotify Developer app. Reuse the same application you already use for **nowPlayingDesktops** and **spot-ai-fy**.

1. Copy the **Client ID** from that app's local config on your machine (it is not stored in those git repos).
2. Paste it into `android/local.properties` (gitignored):

```properties
SPOTIFY_CLIENT_ID=your_spotify_client_id_here
```

Rebuild or sync Gradle so `BuildConfig` picks up the value.

## Redirect URI (already on your Spotify app)

This build uses the **loopback** redirect that is already registered on that Spotify app (no dashboard change):

```text
http://127.0.0.1:8897/callback
```

On the emulator, `127.0.0.1` is the emulator itself. The app starts a short-lived local HTTP listener on port **8897** before opening Spotify's authorize page; when Spotify redirects to that URL, the app receives the authorization code and completes PKCE token exchange. This does not conflict with desktop apps on your laptop.

The custom scheme `com.hippi345.nowplayingwallpaper://callback` is **not** used (Spotify's authorize page rejected it even when listed).

No client secret is used. Tokens are stored only in encrypted on-device preferences.

## Behavior

- Sign in opens Spotify's authorize page in Chrome Custom Tabs.
- While signed in, a **foreground service** polls Spotify's Web API `GET /v1/me/player/currently-playing` every **2 seconds**, including when this app is not on screen (home screen, other apps). You do not need to open the app for the wallpaper to update.
- Wallpaper bitmap is the device’s `widthPixels` × `heightPixels`. A **frosted-glass** backdrop (blurred art + light veil) fills the canvas; the sharp **full cover** is **fit-center** on top. The previous wallpaper stays until the new frame is fully composed, then a single `setBitmap` swap (no black flash). No text on the image.
- When a track is playing, **album art only** (full screen, no title or artist on the wallpaper) is applied with `WallpaperManager`.
- When nothing is playing, or you are signed out, the app shows a clear message — **no sample or invented tracks**.

Playback stays in the Spotify app. This app has **no** play, pause, or skip controls.

## Build

```bash
cd android
./gradlew :domain:test :app:testDebugUnitTest :app:assembleDebug
```
