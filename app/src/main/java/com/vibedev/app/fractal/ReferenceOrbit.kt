package com.vibedev.app.fractal

import java.math.BigDecimal
import java.math.MathContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.ensureActive

/**
 * One orbit of z -> z² + c computed with high precision, then stored as doubles.
 * Every other pixel is drawn as a small offset from this orbit.
 */
class ReferenceOrbit private constructor(val re: DoubleArray, val im: DoubleArray) {
    val lastIndex: Int get() = re.size - 1

    companion object {
        private val BAILOUT = BigDecimal(BAILOUT_SQ)

        suspend fun compute(
            z0Re: BigDecimal,
            z0Im: BigDecimal,
            cRe: BigDecimal,
            cIm: BigDecimal,
            maxIterations: Int,
            mc: MathContext,
        ): ReferenceOrbit {
            val re = DoubleArray(maxIterations + 1)
            val im = DoubleArray(maxIterations + 1)
            var zr = z0Re.round(mc)
            var zi = z0Im.round(mc)
            var count = 0
            while (true) {
                re[count] = zr.toDouble()
                im[count] = zi.toDouble()
                count++
                if (count > maxIterations) break
                val zr2 = zr.multiply(zr, mc)
                val zi2 = zi.multiply(zi, mc)
                // Stop after storing the first point past the bailout, but always keep at least two.
                if (count > 1 && zr2.add(zi2, mc) > BAILOUT) break
                zi = zr.multiply(zi, mc).multiply(BigDecimal.valueOf(2)).add(cIm, mc)
                zr = zr2.subtract(zi2, mc).add(cRe, mc)
                if (count % 64 == 0) coroutineContext.ensureActive()
            }
            return ReferenceOrbit(re.copyOf(count), im.copyOf(count))
        }
    }
}
