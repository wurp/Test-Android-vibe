package com.vibedev.app.fractal

import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min

/**
 * The visible part of the complex plane. [span] is how wide the view is across the
 * screen's shorter side, so it stays correct when the screen size changes.
 */
data class Viewport(val centerRe: Double, val centerIm: Double, val span: Double) {

    fun unitsPerPixel(width: Int, height: Int): Double = span / min(width, height)

    fun toRe(x: Double, width: Int, height: Int): Double =
        centerRe + (x - width / 2.0) * unitsPerPixel(width, height)

    fun toIm(y: Double, width: Int, height: Int): Double =
        centerIm - (y - height / 2.0) * unitsPerPixel(width, height)

    /** Moves the view by a finger drag of (dx, dy) pixels. */
    fun panned(dx: Float, dy: Float, width: Int, height: Int): Viewport {
        val s = unitsPerPixel(width, height)
        return copy(centerRe = centerRe - dx * s, centerIm = centerIm + dy * s)
    }

    /** Zooms by [factor] while keeping the point under (fx, fy) in place. */
    fun zoomed(factor: Float, fx: Float, fy: Float, width: Int, height: Int): Viewport {
        val re = toRe(fx.toDouble(), width, height)
        val im = toIm(fy.toDouble(), width, height)
        val newSpan = span / factor
        val newScale = newSpan / min(width, height)
        return Viewport(
            centerRe = re - (fx - width / 2.0) * newScale,
            centerIm = im + (fy - height / 2.0) * newScale,
            span = newSpan,
        )
    }

    /** More detail is needed the deeper you zoom. */
    fun iterationBudget(): Int {
        val depth = max(0.0, log2(4.0 / span))
        return (200 + depth * 80).toInt().coerceAtMost(6000)
    }

    companion object {
        val MANDELBROT_HOME = Viewport(-0.6, 0.0, 3.2)
        val JULIA_HOME = Viewport(0.0, 0.0, 3.4)
    }
}
