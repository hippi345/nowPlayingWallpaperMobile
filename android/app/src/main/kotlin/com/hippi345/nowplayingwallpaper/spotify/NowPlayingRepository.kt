package com.hippi345.nowplayingwallpaper.spotify

import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack

interface NowPlayingRepository {
    suspend fun currentTrack(): NowPlayingTrack?
}
