package com.hippi345.nowplayingwallpaper.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface WallpaperInstaller {
    suspend fun apply(context: Context, track: NowPlayingTrack)
}

class AndroidWallpaperInstaller(
    private val renderer: WallpaperBitmapRenderer = WallpaperBitmapRenderer(),
) : WallpaperInstaller {
    override suspend fun apply(context: Context, track: NowPlayingTrack) {
        val bitmap = renderer.render(context, track)
        withContext(Dispatchers.Main) {
            WallpaperManager.getInstance(context.applicationContext).setBitmap(bitmap)
        }
        bitmap.recycle()
    }
}
