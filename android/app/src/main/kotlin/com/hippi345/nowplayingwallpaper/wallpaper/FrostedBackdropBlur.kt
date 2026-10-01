package com.hippi345.nowplayingwallpaper.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.math.max
import kotlin.math.min

/**
 * High-resolution frost blur for the wallpaper backdrop (not a tiny proxy upscale).
 */
object FrostedBackdropBlur {
    /** Blur radius in pixels at full canvas resolution (API 31+). */
    private const val FULL_RES_BLUR_RADIUS_PX = 36f

    /** Half-res box blur radius before upscaling (API 26–30). */
    private const val HALF_RES_BOX_RADIUS = 16

    fun blurCenterCropForCanvas(source: Bitmap, canvasWidth: Int, canvasHeight: Int): Bitmap {
        val crop = centerCrop(source, canvasWidth, canvasHeight)
        val blurred = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurWithRenderEffect(crop, FULL_RES_BLUR_RADIUS_PX)
        } else {
            blurAtHalfResolution(crop)
        }
        if (blurred != crop) crop.recycle()
        return blurred
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun blurWithRenderEffect(source: Bitmap, radiusPx: Float): Bitmap {
        val node = RenderNode("frostBackdropBlur")
        node.setPosition(0, 0, source.width, source.height)
        node.setRenderEffect(
            RenderEffect.createBlurEffect(
                radiusPx,
                radiusPx,
                Shader.TileMode.CLAMP,
            ),
        )
        val recording = node.beginRecording(source.width, source.height)
        recording.drawBitmap(source, 0f, 0f, null)
        node.endRecording()
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(output).drawRenderNode(node)
        return output
    }

    private fun blurAtHalfResolution(source: Bitmap): Bitmap {
        val halfW = max(1, source.width / 2)
        val halfH = max(1, source.height / 2)
        val half = Bitmap.createScaledBitmap(source, halfW, halfH, true)
        val blurredHalf = boxBlur(half, HALF_RES_BOX_RADIUS, passes = 3)
        if (half != blurredHalf) half.recycle()
        val full = Bitmap.createScaledBitmap(blurredHalf, source.width, source.height, true)
        blurredHalf.recycle()
        return full
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

    /** Separable box blur (approximates Gaussian); used only below API 31. */
    internal fun boxBlur(source: Bitmap, radius: Int, passes: Int): Bitmap {
        val w = source.width
        val h = source.height
        var pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)
        repeat(passes) {
            pixels = boxBlurHorizontal(pixels, w, h, radius)
            pixels = boxBlurVertical(pixels, w, h, radius)
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    private fun boxBlurHorizontal(pixels: IntArray, w: Int, h: Int, radius: Int): IntArray {
        val out = IntArray(pixels.size)
        val window = radius * 2 + 1
        for (y in 0 until h) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var aSum = 0
            for (i in -radius..radius) {
                val c = pixels[y * w + clamp(i, 0, w - 1)]
                aSum += c ushr 24 and 0xFF
                rSum += c ushr 16 and 0xFF
                gSum += c ushr 8 and 0xFF
                bSum += c and 0xFF
            }
            for (x in 0 until w) {
                val idx = y * w + x
                out[idx] =
                    ((aSum / window) shl 24) or
                    ((rSum / window) shl 16) or
                    ((gSum / window) shl 8) or
                    (bSum / window)
                val removeX = clamp(x - radius, 0, w - 1)
                val addX = clamp(x + radius + 1, 0, w - 1)
                val remove = pixels[y * w + removeX]
                val add = pixels[y * w + addX]
                aSum += (add ushr 24 and 0xFF) - (remove ushr 24 and 0xFF)
                rSum += (add ushr 16 and 0xFF) - (remove ushr 16 and 0xFF)
                gSum += (add ushr 8 and 0xFF) - (remove ushr 8 and 0xFF)
                bSum += (add and 0xFF) - (remove and 0xFF)
            }
        }
        return out
    }

    private fun boxBlurVertical(pixels: IntArray, w: Int, h: Int, radius: Int): IntArray {
        val out = IntArray(pixels.size)
        val window = radius * 2 + 1
        for (x in 0 until w) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var aSum = 0
            for (i in -radius..radius) {
                val c = pixels[clamp(i, 0, h - 1) * w + x]
                aSum += c ushr 24 and 0xFF
                rSum += c ushr 16 and 0xFF
                gSum += c ushr 8 and 0xFF
                bSum += c and 0xFF
            }
            for (y in 0 until h) {
                val idx = y * w + x
                out[idx] =
                    ((aSum / window) shl 24) or
                    ((rSum / window) shl 16) or
                    ((gSum / window) shl 8) or
                    (bSum / window)
                val removeY = clamp(y - radius, 0, h - 1)
                val addY = clamp(y + radius + 1, 0, h - 1)
                val remove = pixels[removeY * w + x]
                val add = pixels[addY * w + x]
                aSum += (add ushr 24 and 0xFF) - (remove ushr 24 and 0xFF)
                rSum += (add ushr 16 and 0xFF) - (remove ushr 16 and 0xFF)
                gSum += (add ushr 8 and 0xFF) - (remove ushr 8 and 0xFF)
                bSum += (add and 0xFF) - (remove and 0xFF)
            }
        }
        return out
    }

    private fun clamp(value: Int, minVal: Int, maxVal: Int): Int = min(max(value, minVal), maxVal)
}
