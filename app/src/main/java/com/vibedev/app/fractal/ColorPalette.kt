package com.vibedev.app.fractal

import kotlin.math.PI
import kotlin.math.cos

/** Turns an escape value into an ARGB color. */
fun interface ColorPalette {
    fun colorFor(escapeValue: Double): Int
}

/** Smooth cycling rainbow-ish palette; points inside the set are black. */
class CosinePalette(private val cycleLength: Double = 48.0) : ColorPalette {
    private val table = IntArray(SIZE) { i ->
        val t = i.toDouble() / SIZE
        val r = channel(t, 0.0)
        val g = channel(t, 0.15)
        val b = channel(t, 0.30)
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun channel(t: Double, phase: Double): Int =
        ((0.5 + 0.5 * cos(2 * PI * (t + phase + 0.6))) * 255).toInt().coerceIn(0, 255)

    override fun colorFor(escapeValue: Double): Int {
        if (escapeValue < 0) return INSIDE_COLOR
        val t = (escapeValue / cycleLength) % 1.0
        return table[(t * SIZE).toInt().coerceIn(0, SIZE - 1)]
    }

    private companion object {
        const val SIZE = 1024
        const val INSIDE_COLOR = 0xFF000000.toInt()
    }
}
