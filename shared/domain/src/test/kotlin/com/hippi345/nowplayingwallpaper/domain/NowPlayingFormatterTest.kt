package com.hippi345.nowplayingwallpaper.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NowPlayingFormatterTest {
    private val sample = NowPlayingTrack(
        title = "Song Title",
        artist = "Artist Name",
        albumName = "Album",
        albumArtUrl = "https://example.com/art.jpg",
    )

    @Test
    fun caption_joinsTitleAndArtist() {
        assertEquals("Song Title — Artist Name", NowPlayingFormatter.caption(sample))
    }

    @Test
    fun accessibilityDescription_includesAlbum() {
        assertEquals(
            "Now playing Song Title by Artist Name from Album",
            NowPlayingFormatter.accessibilityDescription(sample),
        )
    }
}
