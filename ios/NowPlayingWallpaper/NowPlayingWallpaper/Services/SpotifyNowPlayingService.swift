import Foundation

protocol SpotifyNowPlayingService {
    func fetchCurrentTrack() async throws -> NowPlayingTrack?
}
