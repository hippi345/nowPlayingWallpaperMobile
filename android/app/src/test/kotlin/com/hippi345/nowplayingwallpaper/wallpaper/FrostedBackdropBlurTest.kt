package com.hippi345.nowplayingwallpaper.wallpaper

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

class FrostedBackdropBlurTest {
    @Test
    fun blurPixelsForFrost_smoothsHighContrastCheckerboard() {
        val width = 128
        val height = 128
        val cell = 2
        val sharp = checkerboard(width, height, cell)
        val frosted = FrostedBackdropBlur.blurPixelsForFrost(sharp, width, height)

        assertFalse(sharp.contentEquals(frosted), "frost must change pixels")

        val sharpNeighborDelta = maxNeighborLumaDelta(sharp, width, height)
        val frostNeighborDelta = maxNeighborLumaDelta(frosted, width, height)
        assertTrue(
            frostNeighborDelta < sharpNeighborDelta,
            "frost should mix neighbors (sharp=$sharpNeighborDelta frost=$frostNeighborDelta)",
        )

        val centerLuma = luma(frosted[width * height / 2 + width / 2])
        assertTrue(centerLuma in 48..207, "center should blend toward mid-gray, was $centerLuma")

        val uniqueLuma = frosted.map { luma(it) }.toSet().size
        assertTrue(uniqueLuma >= 12, "frost should produce blended luma steps, got $uniqueLuma")
    }

    private fun checkerboard(width: Int, height: Int, cell: Int): IntArray {
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val white = ((x / cell) + (y / cell)) % 2 == 0
                pixels[y * width + x] = if (white) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            }
        }
        return pixels
    }

    private fun maxNeighborLumaDelta(pixels: IntArray, width: Int, height: Int): Int {
        var max = 0
        for (y in 0 until height - 1) {
            for (x in 0 until width - 1) {
                val a = luma(pixels[y * width + x])
                val b = luma(pixels[y * width + x + 1])
                val c = luma(pixels[(y + 1) * width + x])
                max = maxOf(max, abs(a - b), abs(a - c))
            }
        }
        return max
    }

    private fun luma(argb: Int): Int {
        val r = argb shr 16 and 0xFF
        val g = argb shr 8 and 0xFF
        val b = argb and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}
