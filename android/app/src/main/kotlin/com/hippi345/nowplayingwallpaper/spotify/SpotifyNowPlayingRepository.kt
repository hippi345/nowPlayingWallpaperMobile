package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack

class SpotifyNowPlayingRepository(
    private val api: SpotifyWebApiClient,
) : NowPlayingRepository {
    override suspend fun currentTrack(): NowPlayingTrack? {
        return when (val result = api.fetchCurrentlyPlaying()) {
            is SpotifyWebApiClient.CurrentlyPlayingResult.Playing -> result.track
            else -> null
        }
    }

    suspend fun fetchWithStatus(): SpotifyWebApiClient.CurrentlyPlayingResult =
        api.fetchCurrentlyPlaying()
}
