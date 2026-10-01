package com.hippi345.nowplayingwallpaper.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Off-screen compose of frost backdrop + liquid-glass frame (unit-testable).
 */
object WallpaperBitmapComposer {
    fun compose(art: Bitmap, canvasWidth: Int, canvasHeight: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawFrostedFullBackground(canvas, art, canvasWidth, canvasHeight)
        val frame = WallpaperGlassLayout.frameForCanvas(art.width, art.height, canvasWidth, canvasHeight)
        val cover = Bitmap.createScaledBitmap(art, frame.coverWidth, frame.coverHeight, true)
        LiquidGlassFrameDrawer.drawShadow(canvas, frame)
        LiquidGlassFrameDrawer.drawGlassPanel(canvas, frame)
        LiquidGlassFrameDrawer.drawSharpCover(canvas, cover, frame)
        cover.recycle()
        return bitmap
    }

    /** True when [bitmap] includes the dark glass panel (not raw full-bleed cover crop). */
    fun hasGlassPanelMarker(bitmap: Bitmap): Boolean {
        val frame = WallpaperGlassLayout.frameForCanvas(
            sourceWidth = 1000,
            sourceHeight = 1000,
            canvasWidth = bitmap.width,
            canvasHeight = bitmap.height,
        )
        val panelCenterX = (frame.panelLeft + frame.panelWidth / 2f).toInt()
            .coerceIn(0, bitmap.width - 1)
        val panelCenterY = (frame.panelTop + frame.panelHeight / 2f).toInt()
            .coerceIn(0, bitmap.height - 1)
        val panelArgb = bitmap.getPixel(panelCenterX, panelCenterY)
        val cornerX = (bitmap.width * 0.08f).toInt().coerceIn(0, bitmap.width - 1)
        val cornerY = (bitmap.height * 0.08f).toInt().coerceIn(0, bitmap.height - 1)
        val cornerArgb = bitmap.getPixel(cornerX, cornerY)
        return panelIsDarkerThanBackdrop(panelArgb, cornerArgb)
    }

    internal fun panelIsDarkerThanBackdrop(panelArgb: Int, backdropArgb: Int): Boolean {
        fun luma(argb: Int): Int {
            val r = argb shr 16 and 0xFF
            val g = argb shr 8 and 0xFF
            val b = argb and 0xFF
            return r + g + b
        }
        return luma(panelArgb) < luma(backdropArgb) - 40
    }

    private fun drawFrostedFullBackground(canvas: Canvas, source: Bitmap, width: Int, height: Int) {
        val blurred = FrostedBackdropBlur.blurCenterCropForCanvas(source, width, height)
        canvas.drawBitmap(blurred, 0f, 0f, null)
        blurred.recycle()
        val frostVeil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(100, 245, 245, 250)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), frostVeil)
    }
}
