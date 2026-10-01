package com.hippi345.nowplayingwallpaper.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.util.DisplayMetrics
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

        drawBottomGradient(canvas, width, height)
        drawText(canvas, track, width, height, metrics)
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

    private fun drawBottomGradient(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint()
        val gradientHeight = (height * 0.45f).toInt()
        paint.shader = LinearGradient(
            0f,
            (height - gradientHeight).toFloat(),
            0f,
            height.toFloat(),
            intArrayOf(0x00000000, 0xE6000000.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, (height - gradientHeight).toFloat(), width.toFloat(), height.toFloat(), paint)
    }

    private fun drawText(
        canvas: Canvas,
        track: NowPlayingTrack,
        width: Int,
        height: Int,
        metrics: DisplayMetrics,
    ) {
        val padding = (24 * metrics.density).toInt()
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 22f * metrics.scaledDensity
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val artistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE0E0E0.toInt()
            textSize = 16f * metrics.scaledDensity
        }
        val artistY = height - padding.toFloat()
        val titleY = artistY - titlePaint.textSize - (8 * metrics.density)
        canvas.drawText(
            ellipsize(track.title, titlePaint, width - padding * 2),
            padding.toFloat(),
            titleY,
            titlePaint,
        )
        canvas.drawText(
            ellipsize(track.artist, artistPaint, width - padding * 2),
            padding.toFloat(),
            artistY,
            artistPaint,
        )
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Int): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText("${text.take(end)}…") > maxWidth) {
            end--
        }
        return if (end <= 0) "" else "${text.take(end)}…"
    }
}
