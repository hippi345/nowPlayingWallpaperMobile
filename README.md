# nowPlayingWallpaperMobile

Spotify-only phone wallpapers that show the album art, track title, and artist for whatever is playing on Spotify. **Wallpaper only** — no play, pause, or skip on screen. Not a remote control.

| Platform | Path | Status (first slice) |
|----------|------|----------------------|
| Android | [`android/`](android/) | Compose preview UI, JVM-tested domain models, stub Spotify data source |
| iOS | [`ios/`](ios/) | SwiftUI preview UI, stub Spotify data source (build on macOS with Xcode) |

## Requirements

- A [Spotify Developer](https://developer.spotify.com/dashboard) application (Client ID; redirect URIs per platform docs).
- Spotify app installed and signed in on the device.
- **Do not** commit real client secrets, tokens, or keys. Use the example config files.

## Configuration

### Android

Copy `android/local.properties.example` to `android/local.properties` (gitignored) and set:

```properties
SPOTIFY_CLIENT_ID=your_spotify_client_id_here
```

### iOS

Copy `ios/Config/SpotifySecrets.example.xcconfig` to `ios/Config/SpotifySecrets.xcconfig` (gitignored) and set your Client ID. Wire the xcconfig in Xcode per [`ios/README.md`](ios/README.md).

## Development

- Shared Kotlin domain logic: `shared/domain/` (pure JVM, unit-tested on Linux/CI).
- Architecture notes: [`docs/architecture.md`](docs/architecture.md).

### Android (debug)

```bash
cd android
./gradlew :app:assembleDebug :domain:test
```

Release store signing is not configured in this repo.

### iOS

Open `ios/NowPlayingWallpaper/NowPlayingWallpaper.xcodeproj` in Xcode on macOS. Simulator/device builds are not run from this Linux CI slice.

## License

MIT — see [LICENSE](LICENSE).
