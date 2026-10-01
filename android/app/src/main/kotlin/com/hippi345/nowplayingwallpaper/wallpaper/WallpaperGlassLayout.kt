package com.hippi345.nowplayingwallpaper.wallpaper

import kotlin.math.min

/**
 * Liquid-glass frame geometry for the centered album cover (unit-testable).
 */
object WallpaperGlassLayout {
    /** Cover uses this fraction of the largest square that fits on the canvas. */
    const val COVER_MAX_FRACTION = 0.72f

    /** Padding from cover edge to outer glass panel, as a fraction of cover width. */
    const val PANEL_PADDING_FRACTION = 0.07f

    /** Outer panel corner radius as a fraction of cover width. */
    const val PANEL_CORNER_RADIUS_FRACTION = 0.20f

    /** Inner clip radius for the sharp art (slightly tighter than the panel). */
    const val COVER_CORNER_RADIUS_FRACTION = 0.16f

    data class Frame(
        val coverWidth: Int,
        val coverHeight: Int,
        val coverLeft: Float,
        val coverTop: Float,
        val panelLeft: Float,
        val panelTop: Float,
        val panelWidth: Float,
        val panelHeight: Float,
        val panelCornerRadius: Float,
        val coverCornerRadius: Float,
    )

    fun frameForCanvas(
        sourceWidth: Int,
        sourceHeight: Int,
        canvasWidth: Int,
        canvasHeight: Int,
    ): Frame {
        val maxSquare = min(canvasWidth, canvasHeight) * COVER_MAX_FRACTION
        val fitScale = min(
            maxSquare / sourceWidth,
            maxSquare / sourceHeight,
        )
        val coverW = (sourceWidth * fitScale).toInt().coerceAtLeast(1)
        val coverH = (sourceHeight * fitScale).toInt().coerceAtLeast(1)
        val pad = coverW * PANEL_PADDING_FRACTION
        val panelW = coverW + 2f * pad
        val panelH = coverH + 2f * pad
        val panelLeft = (canvasWidth - panelW) / 2f
        val panelTop = (canvasHeight - panelH) / 2f
        val coverLeft = panelLeft + pad
        val coverTop = panelTop + pad
        return Frame(
            coverWidth = coverW,
            coverHeight = coverH,
            coverLeft = coverLeft,
            coverTop = coverTop,
            panelLeft = panelLeft,
            panelTop = panelTop,
            panelWidth = panelW,
            panelHeight = panelH,
            panelCornerRadius = coverW * PANEL_CORNER_RADIUS_FRACTION,
            coverCornerRadius = coverW * COVER_CORNER_RADIUS_FRACTION,
        )
    }
}
