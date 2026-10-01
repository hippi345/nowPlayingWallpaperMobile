package com.hippi345.nowplayingwallpaper.wallpaper

import android.content.Context
import com.hippi345.nowplayingwallpaper.domain.WallpaperLayout

/**
 * Applies rendered wallpaper pixels to the system. Not implemented in the first slice.
 */
interface WallpaperInstaller {
    suspend fun apply(context: Context, layout: WallpaperLayout)
}

class StubWallpaperInstaller : WallpaperInstaller {
    override suspend fun apply(context: Context, layout: WallpaperLayout) {
        // TODO: Render layout to Bitmap and call WallpaperManager.setBitmap(...)
    }
}
