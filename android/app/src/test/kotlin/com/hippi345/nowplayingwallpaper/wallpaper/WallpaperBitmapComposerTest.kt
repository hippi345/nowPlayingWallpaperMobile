package com.hippi345.nowplayingwallpaper.wallpaper

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WallpaperBitmapComposerTest {
    @Test
    fun panelMarker_detectsDarkGlassAgainstFrostedBackdrop() {
        val panel = 0xD2181820.toInt()
        val frost = 0xFFAABBCC.toInt()
        assertTrue(WallpaperBitmapComposer.panelIsDarkerThanBackdrop(panel, frost))
        assertFalse(WallpaperBitmapComposer.panelIsDarkerThanBackdrop(frost, panel))
    }
}
