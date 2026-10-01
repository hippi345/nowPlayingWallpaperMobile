package com.hippi345.nowplayingwallpaper.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.hippi345.nowplayingwallpaper.domain.NowPlayingTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.max

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

        val canvasSize = WallpaperCanvasSize.forDevice(context)
        val width = canvasSize.width
        val height = canvasSize.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawFrostedBackdrop(canvas, art, width, height)
        drawFitCenterCover(canvas, art, width, height)
        art.recycle()
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

    /**
     * Frosted-glass fill: blurred center-crop of the cover plus a light translucent veil (not flat #121212).
     */
    private fun drawFrostedBackdrop(canvas: Canvas, source: Bitmap, width: Int, height: Int) {
        val crop = centerCropBitmap(source, width, height)
        val downW = 56.coerceAtMost(width)
        val downH = (downW * height.toFloat() / width).toInt().coerceAtLeast(1)
        val tiny = Bitmap.createScaledBitmap(crop, downW, downH, true)
        if (crop != source) crop.recycle()
        val blurred = Bitmap.createScaledBitmap(tiny, width, height, true)
        tiny.recycle()
        canvas.drawBitmap(blurred, 0f, 0f, null)
        blurred.recycle()
        val frostVeil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 245, 245, 250)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), frostVeil)
    }

    /** Sharp full cover on top of the frost layer. */
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

    private fun centerCropBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
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
        if (scaled != source) scaled.recycle()
        return cropped
    }
}
