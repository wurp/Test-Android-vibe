package com.vibedev.app.fractal

import android.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Turns an escape value into an ARGB color. */
fun interface ColorPalette {
    fun colorFor(escapeValue: Double): Int
}

/**
 * A palette that repeats every [cycleLength] escape steps, looked up from a precomputed table.
 * Subclasses only describe one cycle via [colorAt]; points inside the set are black.
 */
abstract class CyclingPalette(private val cycleLength: Double) : ColorPalette {
    private val table by lazy { IntArray(SIZE) { colorAt(it.toDouble() / SIZE) } }

    /** Color at position [t] in 0..1 of one cycle. */
    protected abstract fun colorAt(t: Double): Int

    override fun colorFor(escapeValue: Double): Int {
        if (escapeValue < 0) return INSIDE_COLOR
        val t = (escapeValue / cycleLength) % 1.0
        return table[(t * SIZE).toInt().coerceIn(0, SIZE - 1)]
    }

    protected companion object {
        const val SIZE = 1024
        const val INSIDE_COLOR = 0xFF000000.toInt()

        fun rgb(r: Double, g: Double, b: Double): Int =
            (0xFF shl 24) or (byte(r) shl 16) or (byte(g) shl 8) or byte(b)

        private fun byte(v: Double): Int = (v * 255).toInt().coerceIn(0, 255)
    }
}

/** The original soft blue/gold blend. */
class CosinePalette(cycleLength: Double = 48.0) : CyclingPalette(cycleLength) {
    override fun colorAt(t: Double): Int = rgb(channel(t, 0.0), channel(t, 0.15), channel(t, 0.30))

    private fun channel(t: Double, phase: Double) = 0.5 + 0.5 * cos(2 * PI * (t + phase + 0.6))
}

/** Pure spectrum: red, orange, yellow, green, blue, violet, and around again. */
class RainbowPalette(cycleLength: Double = 64.0) : CyclingPalette(cycleLength) {
    override fun colorAt(t: Double): Int = Color.HSVToColor(floatArrayOf((t * 360).toFloat(), 1f, 1f))
}

/** Fast, clashing neon bands: hot pink, acid green, electric blue, with pulsing brightness. */
class PsychedelicPalette(cycleLength: Double = 20.0) : CyclingPalette(cycleLength) {
    override fun colorAt(t: Double): Int {
        val a = 2 * PI * t
        val pulse = 0.65 + 0.35 * sin(5 * a)
        return rgb(
            (0.5 + 0.5 * sin(3 * a)) * pulse + 0.15,
            (0.5 + 0.5 * sin(2 * a + 2.1)) * pulse,
            (0.5 + 0.5 * sin(4 * a + 4.2)) * pulse + 0.15,
        )
    }
}

/** The palettes the user can pick from, in button order. */
enum class PaletteChoice(val label: String, val palette: ColorPalette) {
    CLASSIC("Classic", CosinePalette()),
    RAINBOW("Rainbow", RainbowPalette()),
    PSYCHEDELIC("Psychedelic", PsychedelicPalette()),
    ;

    fun next(): PaletteChoice = entries[(ordinal + 1) % entries.size]
}
