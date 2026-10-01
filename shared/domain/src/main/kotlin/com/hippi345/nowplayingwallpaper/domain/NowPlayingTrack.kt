package com.hippi345.nowplayingwallpaper.domain

/**
 * Metadata shown on the wallpaper. Spotify-only; no playback controls.
 */
data class NowPlayingTrack(
    val spotifyItemId: String? = null,
    val title: String,
    val artist: String,
    val albumName: String,
    val albumArtUrl: String?,
) {
    init {
        require(title.isNotBlank()) { "title must not be blank" }
        require(artist.isNotBlank()) { "artist must not be blank" }
    }

    /** Stable identity for wallpaper refresh when the playing item changes. */
    fun wallpaperIdentity(): String =
        spotifyItemId ?: "$title|$artist|$albumArtUrl"
}
