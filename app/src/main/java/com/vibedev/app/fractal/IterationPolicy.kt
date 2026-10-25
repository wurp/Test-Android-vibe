package com.vibedev.app.fractal

/** What a rendered picture tells us about whether the iteration limit was high enough. */
data class RenderStats(val maxEscape: Int, val insideCount: Int)

/**
 * Picks how many iterations to use. Deep views near the set's edge can need far more than
 * any fixed formula gives, so the limit grows when escaping points crowd up against it.
 */
object IterationPolicy {
    const val MAX_ITERATIONS = 100_000

    fun initial(viewport: Viewport, hint: Int?): Int =
        maxOf(viewport.baselineIterations(), hint ?: 0).coerceAtMost(MAX_ITERATIONS)

    /** A bigger limit if this one looks too low, or null if it's fine. */
    fun raised(current: Int, stats: RenderStats): Int? {
        val tooLow = stats.insideCount > 0 && stats.maxEscape > current / 2
        return if (tooLow && current < MAX_ITERATIONS) minOf(current * 4, MAX_ITERATIONS) else null
    }

    /** What to start the next nearby view with. */
    fun hintAfter(current: Int, stats: RenderStats): Int =
        if (stats.maxEscape > 0) minOf(current, stats.maxEscape * 3) else current
}
