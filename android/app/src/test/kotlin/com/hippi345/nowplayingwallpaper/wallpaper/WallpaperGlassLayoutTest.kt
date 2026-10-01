package com.hippi345.nowplayingwallpaper.wallpaper

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WallpaperGlassLayoutTest {
    @Test
    fun frame_cornerRadius_scalesWithCoverWidth() {
        val frame = WallpaperGlassLayout.frameForCanvas(
            sourceWidth = 1000,
            sourceHeight = 1000,
            canvasWidth = 1080,
            canvasHeight = 2400,
        )
        assertTrue(frame.panelCornerRadius > frame.coverWidth * 0.15f)
        assertTrue(frame.panelWidth > frame.coverWidth)
        assertTrue(frame.coverCornerRadius < frame.panelCornerRadius)
    }
}
