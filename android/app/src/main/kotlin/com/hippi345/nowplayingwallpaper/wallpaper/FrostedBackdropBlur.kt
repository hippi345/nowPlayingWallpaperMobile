package com.hippi345.nowplayingwallpaper.wallpaper

import android.graphics.Bitmap
import kotlin.math.max

/**
 * Frost backdrop: half-resolution center-crop, multi-pass box blur, upscale with filtering.
 * Same pixel path as [blurPixelsForFrost] (unit-tested). No RenderNode / tiny proxy upscale.
 */
object FrostedBackdropBlur {
    private const val HALF_SCALE = 2
    private const val BOX_RADIUS = 18
    private const val BOX_PASSES = 4

    fun blurCenterCropForCanvas(source: Bitmap, canvasWidth: Int, canvasHeight: Int): Bitmap {
        val crop = centerCrop(source, canvasWidth, canvasHeight)
        val blurred = try {
            blurFrostBitmap(crop)
        } catch (_: OutOfMemoryError) {
            val reduced = Bitmap.createScaledBitmap(
                crop,
                max(1, crop.width / 2),
                max(1, crop.height / 2),
                true,
            )
            if (reduced != crop) crop.recycle()
            val smallBlur = blurFrostBitmap(reduced)
            val upscaled = Bitmap.createScaledBitmap(smallBlur, canvasWidth, canvasHeight, true)
            smallBlur.recycle()
            upscaled
        }
        if (blurred != crop) crop.recycle()
        return blurred
    }

    /**
     * Same frost blur the wallpaper uses, on a pixel buffer (for JVM unit tests).
     */
    fun blurPixelsForFrost(pixels: IntArray, width: Int, height: Int): IntArray {
        val halfW = max(1, width / HALF_SCALE)
        val halfH = max(1, height / HALF_SCALE)
        val half = downsampleBoxAverage(pixels, width, height, halfW, halfH)
        val blurredHalf = BoxBlurPixels.blur(half, halfW, halfH, BOX_RADIUS, BOX_PASSES)
        return upsampleBilinear(blurredHalf, halfW, halfH, width, height)
    }

    private fun blurFrostBitmap(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)
        val frosted = blurPixelsForFrost(pixels, w, h)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(frosted, 0, w, 0, 0, w, h)
        return out
    }

    internal fun downsampleBoxAverage(
        pixels: IntArray,
        srcW: Int,
        srcH: Int,
        dstW: Int,
        dstH: Int,
    ): IntArray {
        val out = IntArray(dstW * dstH)
        for (y in 0 until dstH) {
            val y0 = y * srcH / dstH
            val y1 = max(y0 + 1, (y + 1) * srcH / dstH)
            for (x in 0 until dstW) {
                val x0 = x * srcW / dstW
                val x1 = max(x0 + 1, (x + 1) * srcW / dstW)
                var a = 0
                var r = 0
                var g = 0
                var b = 0
                var count = 0
                for (sy in y0 until y1) {
                    for (sx in x0 until x1) {
                        val c = pixels[sy * srcW + sx]
                        a += c ushr 24 and 0xFF
                        r += c ushr 16 and 0xFF
                        g += c ushr 8 and 0xFF
                        b += c and 0xFF
                        count++
                    }
                }
                out[y * dstW + x] =
                    ((a / count) shl 24) or
                    ((r / count) shl 16) or
                    ((g / count) shl 8) or
                    (b / count)
            }
        }
        return out
    }

    internal fun upsampleBilinear(
        pixels: IntArray,
        srcW: Int,
        srcH: Int,
        dstW: Int,
        dstH: Int,
    ): IntArray {
        val out = IntArray(dstW * dstH)
        for (y in 0 until dstH) {
            val gy = (y + 0.5f) * srcH / dstH - 0.5f
            val y0 = gy.toInt().coerceIn(0, srcH - 1)
            val y1 = (y0 + 1).coerceAtMost(srcH - 1)
            val yFrac = gy - y0
            for (x in 0 until dstW) {
                val gx = (x + 0.5f) * srcW / dstW - 0.5f
                val x0 = gx.toInt().coerceIn(0, srcW - 1)
                val x1 = (x0 + 1).coerceAtMost(srcW - 1)
                val xFrac = gx - x0
                val c00 = pixels[y0 * srcW + x0]
                val c10 = pixels[y0 * srcW + x1]
                val c01 = pixels[y1 * srcW + x0]
                val c11 = pixels[y1 * srcW + x1]
                out[y * dstW + x] = lerp4(c00, c10, c01, c11, xFrac, yFrac)
            }
        }
        return out
    }

    private fun lerp4(c00: Int, c10: Int, c01: Int, c11: Int, fx: Float, fy: Float): Int {
        fun ch(c: Int, shift: Int) = (c shr shift) and 0xFF
        fun pack(a: Int, r: Int, g: Int, b: Int) = (a shl 24) or (r shl 16) or (g shl 8) or b
        val a = bilinear(ch(c00, 24), ch(c10, 24), ch(c01, 24), ch(c11, 24), fx, fy)
        val r = bilinear(ch(c00, 16), ch(c10, 16), ch(c01, 16), ch(c11, 16), fx, fy)
        val g = bilinear(ch(c00, 8), ch(c10, 8), ch(c01, 8), ch(c11, 8), fx, fy)
        val b = bilinear(ch(c00, 0), ch(c10, 0), ch(c01, 0), ch(c11, 0), fx, fy)
        return pack(a, r, g, b)
    }

    private fun bilinear(v00: Int, v10: Int, v01: Int, v11: Int, fx: Float, fy: Float): Int {
        val top = v00 + (v10 - v00) * fx
        val bottom = v01 + (v11 - v01) * fx
        return (top + (bottom - top) * fy).toInt().coerceIn(0, 255)
    }

    private fun centerCrop(source: Bitmap, width: Int, height: Int): Bitmap {
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
