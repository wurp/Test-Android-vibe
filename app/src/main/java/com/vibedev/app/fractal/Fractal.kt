package com.vibedev.app.fractal

import kotlin.math.ln

/** A fractal that can tell how quickly a point escapes. */
interface Fractal {
    /**
     * Smooth escape count for the point (re, im), or [INSIDE] if it never escapes
     * within [maxIterations].
     */
    fun escapeValue(re: Double, im: Double, maxIterations: Int): Double

    companion object {
        const val INSIDE = -1.0
    }
}

private const val BAILOUT_SQ = 256.0
private val LN2 = ln(2.0)

/** Iterates z -> z² + c and returns a smooth escape count. Shared by all quadratic fractals. */
internal fun iterateQuadratic(zRe0: Double, zIm0: Double, cRe: Double, cIm: Double, maxIterations: Int): Double {
    var zr = zRe0
    var zi = zIm0
    var zr2 = zr * zr
    var zi2 = zi * zi
    var n = 0
    while (n < maxIterations && zr2 + zi2 <= BAILOUT_SQ) {
        zi = 2 * zr * zi + cIm
        zr = zr2 - zi2 + cRe
        zr2 = zr * zr
        zi2 = zi * zi
        n++
    }
    if (n >= maxIterations) return Fractal.INSIDE
    val logMod = ln(zr2 + zi2) / 2
    return n + 1 - ln(logMod / LN2) / LN2
}

object MandelbrotFractal : Fractal {
    override fun escapeValue(re: Double, im: Double, maxIterations: Int): Double {
        if (isInMainBulbs(re, im)) return Fractal.INSIDE
        return iterateQuadratic(0.0, 0.0, re, im, maxIterations)
    }

    // Quick checks for the big cardioid and period-2 bulb, which are always inside.
    private fun isInMainBulbs(re: Double, im: Double): Boolean {
        val im2 = im * im
        val q = (re - 0.25) * (re - 0.25) + im2
        if (q * (q + (re - 0.25)) <= 0.25 * im2) return true
        return (re + 1) * (re + 1) + im2 <= 0.0625
    }
}

data class JuliaFractal(val cRe: Double, val cIm: Double) : Fractal {
    override fun escapeValue(re: Double, im: Double, maxIterations: Int): Double =
        iterateQuadratic(re, im, cRe, cIm, maxIterations)
}
