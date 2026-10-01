package com.hippi345.nowplayingwallpaper.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.max

/**
 * Wallpaper bitmap matches the launcher’s expected pixel canvas (varies per phone).
 */
data class WallpaperCanvasSize(
    val width: Int,
    val height: Int,
) {
    companion object {
        fun forDevice(context: Context): WallpaperCanvasSize {
            val appContext = context.applicationContext
            val wallpaperManager = WallpaperManager.getInstance(appContext)
            var width = wallpaperManager.desiredMinimumWidth
            var height = wallpaperManager.desiredMinimumHeight

            val display = realDisplaySize(appContext)
            width = max(width, display.x)
            height = max(height, display.y)

            if (width <= 0 || height <= 0) {
                val metrics: DisplayMetrics = appContext.resources.displayMetrics
                width = metrics.widthPixels
                height = metrics.heightPixels
            }

            return WallpaperCanvasSize(
                width = width.coerceAtLeast(1),
                height = height.coerceAtLeast(1),
            )
        }

        private fun realDisplaySize(context: Context): Point {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowManager = context.getSystemService(WindowManager::class.java)
                val bounds = windowManager.maximumWindowMetrics.bounds
                return Point(bounds.width(), bounds.height())
            }
            @Suppress("DEPRECATION")
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            return Point(metrics.widthPixels, metrics.heightPixels)
        }
    }
}
