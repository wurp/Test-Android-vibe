package com.vibedev.app.fractal

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min

/**
 * The visible part of the complex plane. The center is stored with as many digits as the
 * zoom needs. [span] is how wide the view is across the screen's shorter side.
 */
data class Viewport(val centerRe: BigDecimal, val centerIm: BigDecimal, val span: Double) {

    fun unitsPerPixel(width: Int, height: Int): Double = span / min(width, height)

    /** Moves the view by a finger drag of (dx, dy) pixels. */
    fun panned(dx: Float, dy: Float, width: Int, height: Int): Viewport {
        val s = unitsPerPixel(width, height)
        return moved(-dx * s, dy * s, span, width, height)
    }

    /** Zooms by [factor] while keeping the point under (fx, fy) in place. */
    fun zoomed(factor: Float, fx: Float, fy: Float, width: Int, height: Int): Viewport {
        val newSpan = (span / factor).coerceIn(MIN_SPAN, MAX_SPAN)
        val shrink = unitsPerPixel(width, height) - newSpan / min(width, height)
        return moved((fx - width / 2.0) * shrink, -(fy - height / 2.0) * shrink, newSpan, width, height)
    }

    private fun moved(dRe: Double, dIm: Double, newSpan: Double, width: Int, height: Int): Viewport {
        val places = ceil(-log10(newSpan / min(width, height))).toInt() + 8
        return Viewport(
            centerRe.add(BigDecimal(dRe, OFFSET_MC)).setScale(max(places, 4), RoundingMode.HALF_EVEN),
            centerIm.add(BigDecimal(dIm, OFFSET_MC)).setScale(max(places, 4), RoundingMode.HALF_EVEN),
            newSpan,
        )
    }

    /** How far right/down (in pixels of this view) [other]'s center sits from ours. */
    fun pixelOffsetOf(other: Viewport, width: Int, height: Int): Pair<Double, Double> {
        val s = unitsPerPixel(width, height)
        return other.centerRe.subtract(centerRe).toDouble() / s to
            -other.centerIm.subtract(centerIm).toDouble() / s
    }

    /** Digits needed for the reference point at this zoom. */
    fun mathContext(): MathContext = MathContext(max(24, ceil(-log10(span)).toInt() + 24))

    /** A starting guess for how many iterations this zoom needs; [IterationPolicy] refines it. */
    fun baselineIterations(): Int {
        val depth = max(0.0, log2(4.0 / span))
        return (250 + depth * 50).toInt()
    }

    fun zoom(): Double = MANDELBROT_HOME.span / span

    companion object {
        /** About 10^280 times zoomed in; deeper than this, ordinary numbers run out of room. */
        const val MIN_SPAN = 1e-280
        const val MAX_SPAN = 16.0
        private val OFFSET_MC = MathContext(17)

        val MANDELBROT_HOME = Viewport(BigDecimal("-0.6"), BigDecimal.ZERO, 3.2)
        val JULIA_HOME = Viewport(BigDecimal.ZERO, BigDecimal.ZERO, 3.4)
    }
}
