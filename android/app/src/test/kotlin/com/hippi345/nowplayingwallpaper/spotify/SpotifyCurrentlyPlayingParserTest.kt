package com.hippi345.nowplayingwallpaper.spotify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SpotifyCurrentlyPlayingParserTest {
    @Test
    fun parse_returnsTrackWhenPlaying() {
        val json = """
            {
              "is_playing": true,
              "item": {
                "name": "Track One",
                "artists": [{ "name": "Artist A" }],
                "album": {
                  "name": "Album X",
                  "images": [{ "url": "https://i.scdn.co/image/example" }]
                }
              }
            }
        """.trimIndent()
        val track = SpotifyCurrentlyPlayingParser.parse(json)
        assertEquals("Track One", track?.title)
        assertEquals("Artist A", track?.artist)
        assertEquals("https://i.scdn.co/image/example", track?.albumArtUrl)
    }

    @Test
    fun parse_returnsNullWhenNotPlaying() {
        val json = """{ "is_playing": false, "item": { "name": "X", "artists": [{ "name": "Y" }] } }"""
        assertNull(SpotifyCurrentlyPlayingParser.parse(json))
    }
}
