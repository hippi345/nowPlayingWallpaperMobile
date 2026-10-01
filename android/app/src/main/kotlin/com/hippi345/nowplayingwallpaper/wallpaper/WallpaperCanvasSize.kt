package com.hippi345.nowplayingwallpaper.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.util.DisplayMetrics

/**
 * Launcher wallpaper bitmap size: at least [WallpaperManager.getDesiredMinimumWidth/Height],
 * which is typically wider than the visible screen (parallax). Album art is fit-center on this canvas.
 */
data class WallpaperCanvasSize(
    val width: Int,
    val height: Int,
) {
    companion object {
        fun forDevice(context: Context): WallpaperCanvasSize {
            val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
            val metrics: DisplayMetrics = context.resources.displayMetrics
            val width = wallpaperManager.desiredMinimumWidth
                .takeIf { it > 0 }
                ?: metrics.widthPixels
            val height = wallpaperManager.desiredMinimumHeight
                .takeIf { it > 0 }
                ?: metrics.heightPixels
            return WallpaperCanvasSize(width = width, height = height)
        }
    }
}
