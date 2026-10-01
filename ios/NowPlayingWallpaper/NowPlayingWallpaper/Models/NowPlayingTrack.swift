import Foundation

/// Metadata for the wallpaper. Spotify only — no transport controls.
struct NowPlayingTrack: Equatable {
    let title: String
    let artist: String
    let albumName: String
    let albumArtURL: URL?

    var wallpaperHeadline: String { title }
    var wallpaperSubline: String { artist }
}
