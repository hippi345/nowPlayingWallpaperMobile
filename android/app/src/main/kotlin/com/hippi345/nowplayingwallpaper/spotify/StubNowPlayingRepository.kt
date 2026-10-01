package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack

/**
 * Placeholder until Spotify App Remote / Web API integration lands.
 */
class StubNowPlayingRepository : NowPlayingRepository {
    override suspend fun currentTrack(): NowPlayingTrack =
        NowPlayingTrack(
            title = "Example Track",
            artist = "Example Artist",
            albumName = "Example Album",
            albumArtUrl = null,
        )
}
