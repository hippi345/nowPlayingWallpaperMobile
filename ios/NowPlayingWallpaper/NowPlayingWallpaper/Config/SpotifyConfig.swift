import Foundation

enum SpotifyConfig {
    /// Injected via `SpotifySecrets.xcconfig` → Info.plist or build setting.
    static var clientId: String {
        Bundle.main.object(forInfoDictionaryKey: "SPOTIFY_CLIENT_ID") as? String
            ?? "your_spotify_client_id_here"
    }

    static var isPlaceholder: Bool {
        clientId.isEmpty || clientId == "your_spotify_client_id_here"
    }
}
