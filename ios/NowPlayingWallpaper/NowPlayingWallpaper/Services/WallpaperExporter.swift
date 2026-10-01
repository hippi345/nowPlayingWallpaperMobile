import UIKit

/// Renders and saves wallpaper images. System wallpaper APIs are limited on iOS.
enum WallpaperExporter {
    static func exportPlaceholder(for track: NowPlayingTrack) async throws {
        // TODO: Render UIImage from track metadata and write to Photo Library.
        _ = track
    }
}
