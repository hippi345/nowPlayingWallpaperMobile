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

    private fun drawFrostedBackdrop(canvas: Canvas, source: Bitmap, width: Int, height: Int) {
        val blurred = FrostedBackdropBlur.blurCenterCropForCanvas(source, width, height)
        canvas.drawBitmap(blurred, 0f, 0f, null)
        blurred.recycle()
        val frostVeil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(115, 245, 245, 250)
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
}
