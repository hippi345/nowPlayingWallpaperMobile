# iOS — Now Playing Wallpaper

SwiftUI app that previews the Spotify now-playing wallpaper layout. **Spotify only**; no playback controls on the wallpaper.

## Setup

1. Install Xcode 15+ on macOS.
2. Copy `Config/SpotifySecrets.example.xcconfig` to `Config/SpotifySecrets.xcconfig` (gitignored).
3. Set `SPOTIFY_CLIENT_ID = your_spotify_client_id_here` in the xcconfig.
4. Open `NowPlayingWallpaper/NowPlayingWallpaper.xcodeproj`.
5. In the project build settings, set **Based on Configuration File** to include `SpotifySecrets.xcconfig` for Debug/Release (File → Project Settings → Info → Configurations).

## First slice status

| Area | Status |
|------|--------|
| SwiftUI wallpaper preview | Implemented |
| `SpotifyNowPlayingService` | Stub sample track |
| Spotify iOS SDK auth | Not wired |
| Export / apply wallpaper image | Stub (`WallpaperExporter`) |

iOS does not allow third-party apps to set the home-screen wallpaper directly like Android; the exporter will save a rendered image for the user to apply manually (or via Shortcuts in a later slice).

## Build

```bash
# On macOS with Xcode:
xcodebuild -project NowPlayingWallpaper/NowPlayingWallpaper.xcodeproj \
  -scheme NowPlayingWallpaper \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  build
```

This slice is not built on Linux CI.
