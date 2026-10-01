import SwiftUI

struct WallpaperPreviewView: View {
    @ObservedObject var viewModel: NowPlayingViewModel

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    ProgressView("Loading…")
                } else if let track = viewModel.track {
                    wallpaperCard(for: track)
                } else {
                    Text("No track")
                }
            }
            .navigationTitle("Wallpaper preview")
            .task { await viewModel.load() }
        }
    }

    @ViewBuilder
    private func wallpaperCard(for track: NowPlayingTrack) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Spotify only. Wallpaper display — no playback controls.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Text("Showing stub now playing data. Spotify sign-in not wired yet.")
                .font(.caption)
                .foregroundStyle(.secondary)

            ZStack(alignment: .bottomLeading) {
                LinearGradient(
                    colors: [Color(red: 0.07, green: 0.07, blue: 0.07), Color(red: 0.11, green: 0.73, blue: 0.33)],
                    startPoint: .top,
                    endPoint: .bottom
                )
                VStack(alignment: .leading, spacing: 12) {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color(white: 0.2))
                        .frame(width: 120, height: 120)
                        .overlay {
                            Text("♫")
                                .font(.largeTitle)
                                .foregroundStyle(.white)
                        }
                    Text(track.wallpaperHeadline)
                        .font(.title2.bold())
                        .foregroundStyle(.white)
                        .lineLimit(2)
                    Text(track.wallpaperSubline)
                        .font(.title3)
                        .foregroundStyle(Color(white: 0.88))
                        .lineLimit(1)
                }
                .padding(24)
            }
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .frame(maxWidth: .infinity, minHeight: 360)

            Text("No play, pause, or skip — wallpaper only.")
                .font(.footnote)
                .frame(maxWidth: .infinity)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
        }
        .padding()
    }
}
