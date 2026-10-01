package com.hippi345.nowplayingwallpaper.wallpaper

import android.content.Context
import android.util.DisplayMetrics

/**
 * Wallpaper bitmap matches the device’s visible screen in pixels (varies per phone).
 */
data class WallpaperCanvasSize(
    val width: Int,
    val height: Int,
) {
    companion object {
        fun forDevice(context: Context): WallpaperCanvasSize {
            val metrics: DisplayMetrics = context.resources.displayMetrics
            return WallpaperCanvasSize(
                width = metrics.widthPixels,
                height = metrics.heightPixels,
            )
        }
    }
}
