package com.hippi345.nowplayingwallpaper.wallpaper

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

object LiquidGlassFrameDrawer {
    fun drawShadow(canvas: Canvas, frame: WallpaperGlassLayout.Frame) {
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(110, 0, 0, 0)
            maskFilter = BlurMaskFilter(frame.panelCornerRadius * 0.65f, BlurMaskFilter.Blur.NORMAL)
        }
        val offsetY = frame.panelCornerRadius * 0.35f
        canvas.drawRoundRect(
            frame.panelLeft,
            frame.panelTop + offsetY,
            frame.panelLeft + frame.panelWidth,
            frame.panelTop + frame.panelHeight + offsetY,
            frame.panelCornerRadius,
            frame.panelCornerRadius,
            shadowPaint,
        )
    }

    fun drawGlassPanel(canvas: Canvas, frame: WallpaperGlassLayout.Frame) {
        val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(210, 24, 24, 32)
        }
        canvas.drawRoundRect(
            frame.panelLeft,
            frame.panelTop,
            frame.panelLeft + frame.panelWidth,
            frame.panelTop + frame.panelHeight,
            frame.panelCornerRadius,
            frame.panelCornerRadius,
            panelPaint,
        )
        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = maxOf(2f, frame.panelWidth * 0.004f)
            color = Color.argb(130, 255, 255, 255)
        }
        val inset = edgePaint.strokeWidth / 2f
        canvas.drawRoundRect(
            frame.panelLeft + inset,
            frame.panelTop + inset,
            frame.panelLeft + frame.panelWidth - inset,
            frame.panelTop + frame.panelHeight - inset,
            frame.panelCornerRadius,
            frame.panelCornerRadius,
            edgePaint,
        )
    }

    fun drawSharpCover(canvas: Canvas, cover: Bitmap, frame: WallpaperGlassLayout.Frame) {
        val path = Path()
        val rect = RectF(
            frame.coverLeft,
            frame.coverTop,
            frame.coverLeft + frame.coverWidth,
            frame.coverTop + frame.coverHeight,
        )
        path.addRoundRect(rect, frame.coverCornerRadius, frame.coverCornerRadius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(cover, frame.coverLeft, frame.coverTop, null)
        canvas.restore()
    }
}
