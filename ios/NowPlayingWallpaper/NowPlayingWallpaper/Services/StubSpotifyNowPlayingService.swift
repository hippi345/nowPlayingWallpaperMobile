import Foundation

/// Placeholder until Spotify iOS SDK integration.
struct StubSpotifyNowPlayingService: SpotifyNowPlayingService {
    func fetchCurrentTrack() async throws -> NowPlayingTrack? {
        NowPlayingTrack(
            title: "Example Track",
            artist: "Example Artist",
            albumName: "Example Album",
            albumArtURL: nil
        )
    }
}
