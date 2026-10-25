package com.vibedev.app.fractal

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.ln
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerturbationTest {

    /** Plain high-precision iteration, slow but trustworthy. Returns the escape iteration or -1. */
    private fun exactEscape(z0r: BigDecimal, z0i: BigDecimal, cr: BigDecimal, ci: BigDecimal, maxIter: Int, mc: MathContext): Int {
        var zr = z0r
        var zi = z0i
        for (n in 0..maxIter) {
            val zr2 = zr.multiply(zr, mc)
            val zi2 = zi.multiply(zi, mc)
            if (zr2.add(zi2, mc).toDouble() > BAILOUT_SQ) return n
            zi = zr.multiply(zi, mc).multiply(BigDecimal(2)).add(ci, mc)
            zr = zr2.subtract(zi2, mc).add(cr, mc)
        }
        return -1
    }

    private fun bd(d: Double) = BigDecimal(d)

    private fun assertMatches(plan: PerturbationPlan, dRe: Double, dIm: Double, exact: Int) {
        val v = plan.escapeValue(dRe, dIm)
        if (exact < 0) {
            assertTrue("expected inside, got $v", v < 0)
        } else {
            // Smooth value is n + 1 - log2(log2|z|), and |z| is between 16 and 256 at escape.
            assertTrue("expected escape near $exact, got $v", v > exact - 2.01 && v <= exact - 0.99)
        }
    }

    @Test
    fun mandelbrotMatchesExactAtHomeView() = runBlocking {
        val view = Viewport.MANDELBROT_HOME
        val plan = MandelbrotFractal.plan(view, view.baselineIterations())
        val rnd = Random(1)
        repeat(300) {
            val dRe = (rnd.nextDouble() - 0.5) * 3.2
            val dIm = (rnd.nextDouble() - 0.5) * 3.2
            val mc = MathContext(40)
            val exact = exactEscape(BigDecimal.ZERO, BigDecimal.ZERO, view.centerRe.add(bd(dRe)), view.centerIm.add(bd(dIm)), plan.maxIterations, mc)
            assertMatches(plan, dRe, dIm, exact)
        }
    }

    @Test
    fun juliaMatchesExactAtHomeView() = runBlocking {
        val c = JuliaFractal(BigDecimal("-0.7269"), BigDecimal("0.1889"))
        val view = Viewport.JULIA_HOME
        val plan = c.plan(view, view.baselineIterations())
        val rnd = Random(2)
        repeat(300) {
            val dRe = (rnd.nextDouble() - 0.5) * 3.4
            val dIm = (rnd.nextDouble() - 0.5) * 3.4
            val exact = exactEscape(bd(dRe), bd(dIm), c.cRe, c.cIm, plan.maxIterations, MathContext(40))
            assertMatches(plan, dRe, dIm, exact)
        }
    }

    @Test
    fun mandelbrotMatchesExactWhenZoomedDeep() = runBlocking {
        val view = Viewport(
            BigDecimal("-0.743643887037158704752191506114774"),
            BigDecimal("0.131825904205311970493132056385139"),
            1e-28,
        )
        val plan = MandelbrotFractal.plan(view, 60_000)
        val rnd = Random(3)
        val values = mutableSetOf<Double>()
        repeat(20) {
            val dRe = (rnd.nextDouble() - 0.5) * view.span
            val dIm = (rnd.nextDouble() - 0.5) * view.span
            val exact = exactEscape(
                BigDecimal.ZERO, BigDecimal.ZERO,
                view.centerRe.add(bd(dRe)), view.centerIm.add(bd(dIm)),
                plan.maxIterations, MathContext(60),
            )
            assertMatches(plan, dRe, dIm, exact)
            values += plan.escapeValue(dRe, dIm)
        }
        assertTrue("pixels should differ, not be blocky: $values", values.size > 15)
    }

    @Test
    fun zoomKeepsFocusPointFixed() {
        val v = Viewport.MANDELBROT_HOME
        val z = v.zoomed(1e6f, 100f, 300f, 1000, 2000)
        // The point under the finger was at (100-500, 300-1000) pixels from center.
        val s0 = v.unitsPerPixel(1000, 2000)
        val s1 = z.unitsPerPixel(1000, 2000)
        val beforeRe = v.centerRe.toDouble() + (100 - 500) * s0
        val afterRe = z.centerRe.toDouble() + (100 - 500) * s1
        assertEquals(beforeRe, afterRe, 1e-12)
        val beforeIm = v.centerIm.toDouble() - (300 - 1000) * s0
        val afterIm = z.centerIm.toDouble() - (300 - 1000) * s1
        assertEquals(beforeIm, afterIm, 1e-12)
        assertEquals(ln(1e6), ln(v.span / z.span), 1e-3)
    }
}

class IterationPolicyTest {
    @Test
    fun raisesLimitWhenEscapesCrowdTheLimit() {
        assertEquals(4000, IterationPolicy.raised(1000, RenderStats(maxEscape = 900, insideCount = 5)))
        assertEquals(null, IterationPolicy.raised(1000, RenderStats(maxEscape = 300, insideCount = 5)))
        assertEquals(null, IterationPolicy.raised(1000, RenderStats(maxEscape = 900, insideCount = 0)))
    }
}
