package com.hippi345.nowplayingwallpaper.domain

/**
 * Describes what to paint on the wallpaper surface (no transport UI).
 */
data class WallpaperLayout(
    val headline: String,
    val subline: String,
    val albumArtUrl: String?,
) {
    companion object {
        fun fromTrack(track: NowPlayingTrack): WallpaperLayout =
            WallpaperLayout(
                headline = track.title,
                subline = track.artist,
                albumArtUrl = track.albumArtUrl,
            )
    }
}
