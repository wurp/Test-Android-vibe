package com.vibedev.app.fractal

import kotlin.math.ln

internal const val BAILOUT_SQ = 256.0
private val LN2 = ln(2.0)

/**
 * Everything needed to color pixels near a reference point.
 *
 * Pixels are given as an offset (dRe, dIm) from the view center. For the Mandelbrot set the
 * offset changes c; for a Julia set it changes the starting z.
 *
 * When the offset gets large compared to the orbit, it "rebases" onto [rebase], which starts
 * at 0. This avoids the glitches plain perturbation is known for.
 */
class PerturbationPlan(
    private val primary: ReferenceOrbit,
    private val rebase: ReferenceOrbit,
    private val offsetIsC: Boolean,
    val maxIterations: Int,
) {
    /** Smooth escape count, or [INSIDE] if the point never escapes. */
    fun escapeValue(dRe: Double, dIm: Double): Double {
        val dcR = if (offsetIsC) dRe else 0.0
        val dcI = if (offsetIsC) dIm else 0.0
        var dzR = if (offsetIsC) 0.0 else dRe
        var dzI = if (offsetIsC) 0.0 else dIm
        var orbit = primary
        var m = 0

        var zr = orbit.re[0] + dzR
        var zi = orbit.im[0] + dzI
        if (zr * zr + zi * zi > BAILOUT_SQ) return smooth(0, zr * zr + zi * zi)

        var n = 0
        while (n < maxIterations) {
            val refR = orbit.re[m]
            val refI = orbit.im[m]
            val nR = 2 * (refR * dzR - refI * dzI) + (dzR * dzR - dzI * dzI) + dcR
            val nI = 2 * (refR * dzI + refI * dzR) + 2 * dzR * dzI + dcI
            dzR = nR
            dzI = nI
            m++
            n++
            zr = orbit.re[m] + dzR
            zi = orbit.im[m] + dzI
            val mag = zr * zr + zi * zi
            if (mag > BAILOUT_SQ) return smooth(n, mag)
            if (mag < dzR * dzR + dzI * dzI || m == orbit.lastIndex) {
                dzR = zr
                dzI = zi
                orbit = rebase
                m = 0
            }
        }
        return INSIDE
    }

    private fun smooth(n: Int, magSq: Double): Double = n + 1 - ln(ln(magSq) / 2 / LN2) / LN2

    companion object {
        const val INSIDE = -1.0
    }
}
