package com.hippi345.nowplayingwallpaper.wallpaper

import kotlin.math.min

/**
 * Layout math for album art on the launcher wallpaper canvas (no Android deps — unit-testable).
 */
object WallpaperArtLayout {
    /** Scale so the entire source image fits inside the canvas (FIT_CENTER / contain). */
    fun fitCenterScale(sourceWidth: Int, sourceHeight: Int, canvasWidth: Int, canvasHeight: Int): Float {
        if (sourceWidth <= 0 || sourceHeight <= 0) return 1f
        return min(
            canvasWidth.toFloat() / sourceWidth,
            canvasHeight.toFloat() / sourceHeight,
        )
    }

    fun fitCenterSize(
        sourceWidth: Int,
        sourceHeight: Int,
        canvasWidth: Int,
        canvasHeight: Int,
    ): Pair<Int, Int> {
        val scale = fitCenterScale(sourceWidth, sourceHeight, canvasWidth, canvasHeight)
        return Pair(
            (sourceWidth * scale).toInt().coerceAtLeast(1),
            (sourceHeight * scale).toInt().coerceAtLeast(1),
        )
    }
}
