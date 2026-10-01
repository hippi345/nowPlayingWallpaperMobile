package com.hippi345.nowplayingwallpaper.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NowPlayingTrackTest {
    @Test
    fun wallpaperIdentity_prefersSpotifyItemId() {
        val track = NowPlayingTrack(
            spotifyItemId = "abc123",
            title = "T",
            artist = "A",
            albumName = "Al",
            albumArtUrl = "https://example.com/x.jpg",
        )
        assertEquals("abc123", track.wallpaperIdentity())
    }
}
