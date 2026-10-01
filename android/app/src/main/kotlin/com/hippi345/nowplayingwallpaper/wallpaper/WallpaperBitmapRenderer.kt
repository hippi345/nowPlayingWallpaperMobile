package com.hippi345.nowplayingwallpaper.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class WallpaperBitmapRenderer(
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    /**
     * Builds the full wallpaper off-screen. Returns null if art cannot be loaded — never a blank/black frame.
     */
    suspend fun render(
        context: Context,
        track: NowPlayingTrack,
    ): Bitmap? = withContext(Dispatchers.Default) {
        val artUrl = track.albumArtUrl ?: return@withContext null
        val art = downloadBitmap(artUrl) ?: return@withContext null
        try {
            val canvasSize = WallpaperCanvasSize.forDevice(context)
            WallpaperBitmapComposer.compose(art, canvasSize.width, canvasSize.height)
        } finally {
            art.recycle()
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val bytes = response.body?.bytes() ?: return null
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}
