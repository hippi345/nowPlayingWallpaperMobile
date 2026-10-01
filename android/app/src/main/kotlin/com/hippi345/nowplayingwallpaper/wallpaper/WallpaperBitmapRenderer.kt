package com.hippi345.nowplayingwallpaper.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class WallpaperBitmapRenderer(
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    suspend fun render(
        context: Context,
        track: NowPlayingTrack,
    ): Bitmap = withContext(Dispatchers.IO) {
        val canvasSize = WallpaperCanvasSize.forDevice(context)
        val width = canvasSize.width
        val height = canvasSize.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(LETTERBOX_COLOR)

        val art = track.albumArtUrl?.let { downloadBitmap(it) }
        if (art != null) {
            drawFitCenterCover(canvas, art, width, height)
            art.recycle()
        }
        bitmap
    }

    private fun downloadBitmap(url: String): Bitmap? {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val bytes = response.body?.bytes() ?: return null
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    /** Full square (or rectangular) cover visible, scaled up to the largest size that fits — no crop, no blur. */
    private fun drawFitCenterCover(canvas: Canvas, source: Bitmap, width: Int, height: Int) {
        val (scaledW, scaledH) = WallpaperArtLayout.fitCenterSize(
            source.width,
            source.height,
            width,
            height,
        )
        val scaled = Bitmap.createScaledBitmap(source, scaledW, scaledH, true)
        val left = (width - scaledW) / 2f
        val top = (height - scaledH) / 2f
        canvas.drawBitmap(scaled, left, top, null)
        scaled.recycle()
    }

    companion object {
        private const val LETTERBOX_COLOR = 0xFF121212.toInt()
    }
}
