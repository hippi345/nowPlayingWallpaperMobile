import SwiftUI

@main
struct NowPlayingWallpaperApp: App {
    @StateObject private var viewModel = NowPlayingViewModel()

    var body: some Scene {
        WindowGroup {
            WallpaperPreviewView(viewModel: viewModel)
        }
    }
}
