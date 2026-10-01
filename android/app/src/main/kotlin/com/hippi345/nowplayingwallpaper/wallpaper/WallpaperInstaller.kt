package com.hippi345.nowplayingwallpaper.wallpaper

import android.app.WallpaperManager
import android.content.Context
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
                WallpaperManager.getInstance(context.applicationContext).setBitmap(bitmap)
            }
            bitmap.recycle()
            true
        }
}
