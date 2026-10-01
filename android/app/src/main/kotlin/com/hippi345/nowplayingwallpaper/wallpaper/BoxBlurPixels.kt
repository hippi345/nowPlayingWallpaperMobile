package com.hippi345.nowplayingwallpaper.wallpaper

import kotlin.math.max
import kotlin.math.min

/**
 * Separable box blur on raw ARGB pixels (JVM-testable; used for the frost backdrop).
 */
object BoxBlurPixels {
    fun blur(pixels: IntArray, width: Int, height: Int, radius: Int, passes: Int): IntArray {
        var data = pixels
        repeat(passes) {
            data = boxBlurHorizontal(data, width, height, radius)
            data = boxBlurVertical(data, width, height, radius)
        }
        return data
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
