package com.hippi345.nowplayingwallpaper.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

interface WallpaperInstaller {
    /** @return true if a new wallpaper was applied */
    suspend fun apply(context: Context, track: NowPlayingTrack): Boolean
}

class AndroidWallpaperInstaller(
    private val renderer: WallpaperBitmapRenderer = WallpaperBitmapRenderer(),
) : WallpaperInstaller {
    private val composeMutex = Mutex()

    override suspend fun apply(context: Context, track: NowPlayingTrack): Boolean =
        composeMutex.withLock {
            val bitmap = renderer.render(context, track)
                ?: return false
            withContext(Dispatchers.Main) {
                applyWallpaper(context.applicationContext, bitmap)
            }
            bitmap.recycle()
            true
        }

    private fun applyWallpaper(context: Context, bitmap: android.graphics.Bitmap) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wallpaperManager.setBitmap(
                bitmap,
                null,
                true,
                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
            )
        } else {
            wallpaperManager.setBitmap(bitmap)
        }
    }
}
