import Foundation

@MainActor
final class NowPlayingViewModel: ObservableObject {
    @Published private(set) var track: NowPlayingTrack?
    @Published private(set) var isLoading = true

    private let service: SpotifyNowPlayingService

    init(service: SpotifyNowPlayingService = StubSpotifyNowPlayingService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        defer { isLoading = false }
        track = try? await service.fetchCurrentTrack()
    }
}
