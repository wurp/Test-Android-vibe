package com.vibedev.app.fractal

import java.math.BigDecimal

/** A fractal that can prepare to draw a given view. */
interface Fractal {
    suspend fun plan(viewport: Viewport, maxIterations: Int): PerturbationPlan
}

object MandelbrotFractal : Fractal {
    override suspend fun plan(viewport: Viewport, maxIterations: Int): PerturbationPlan {
        val maxIter = maxIterations
        val orbit = ReferenceOrbit.compute(
            BigDecimal.ZERO, BigDecimal.ZERO,
            viewport.centerRe, viewport.centerIm,
            maxIter, viewport.mathContext(),
        )
        return PerturbationPlan(orbit, orbit, offsetIsC = true, maxIterations = maxIter)
    }
}

/** The Julia set for the constant c = cRe + cIm·i. */
data class JuliaFractal(val cRe: BigDecimal, val cIm: BigDecimal) : Fractal {
    override suspend fun plan(viewport: Viewport, maxIterations: Int): PerturbationPlan {
        val maxIter = maxIterations
        val mc = viewport.mathContext()
        val primary = ReferenceOrbit.compute(viewport.centerRe, viewport.centerIm, cRe, cIm, maxIter, mc)
        // The orbit of 0 is used after a rebase, since rebasing needs an orbit that starts at 0.
        val critical = ReferenceOrbit.compute(BigDecimal.ZERO, BigDecimal.ZERO, cRe, cIm, maxIter, mc)
        return PerturbationPlan(primary, critical, offsetIsC = false, maxIterations = maxIter)
    }
}
