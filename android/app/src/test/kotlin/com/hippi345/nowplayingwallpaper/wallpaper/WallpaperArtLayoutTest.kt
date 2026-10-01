package com.hippi345.nowplayingwallpaper.wallpaper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WallpaperArtLayoutTest {
    @Test
    fun fitCenter_squareCoverOnTallCanvas_usesFullWidth() {
        val (w, h) = WallpaperArtLayout.fitCenterSize(1000, 1000, 1440, 3200)
        assertEquals(1440, w)
        assertEquals(1440, h)
    }
}
