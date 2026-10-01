package com.hippi345.nowplayingwallpaper.sync

import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import com.hippi345.nowplayingwallpaper.domain.WallpaperLayout

data class WallpaperSyncState(
    val loading: Boolean = true,
    val statusMessage: String? = null,
    val layout: WallpaperLayout? = null,
    val lastWallpaperTrack: NowPlayingTrack? = null,
)
