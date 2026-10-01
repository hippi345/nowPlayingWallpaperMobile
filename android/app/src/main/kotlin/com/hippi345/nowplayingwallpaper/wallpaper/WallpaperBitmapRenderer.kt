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
import kotlin.math.max

class WallpaperBitmapRenderer(
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    suspend fun render(
        context: Context,
        track: NowPlayingTrack,
    ): Bitmap = withContext(Dispatchers.IO) {
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val art = track.albumArtUrl?.let { downloadBitmap(it) }
        if (art != null) {
            drawCenterCrop(canvas, art, width, height)
            art.recycle()
        } else {
            canvas.drawColor(0xFF121212.toInt())
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

    private fun drawCenterCrop(canvas: Canvas, source: Bitmap, width: Int, height: Int) {
        val scale = max(width.toFloat() / source.width, height.toFloat() / source.height)
        val scaledW = (source.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, scaledW, scaledH, true)
        val left = ((scaledW - width) / 2).coerceAtLeast(0)
        val top = ((scaledH - height) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(
            scaled,
            left,
            top,
            width.coerceAtMost(scaledW),
            height.coerceAtMost(scaledH),
        )
        canvas.drawBitmap(cropped, 0f, 0f, null)
        if (scaled != source) scaled.recycle()
        cropped.recycle()
    }
}
