package com.hippi345.nowplayingwallpaper.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WallpaperLayoutTest {
    @Test
    fun fromTrack_mapsFields() {
        val track = NowPlayingTrack(
            title = "A",
            artist = "B",
            albumName = "C",
            albumArtUrl = null,
        )
        val layout = WallpaperLayout.fromTrack(track)
        assertEquals("A", layout.headline)
        assertEquals("B", layout.subline)
    }
}
