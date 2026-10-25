package com.vibedev.app.fractal

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** A finished picture plus the view and screen size it was drawn for. */
class RenderedFrame(
    val fractal: Fractal,
    val image: ImageBitmap,
    val viewport: Viewport,
    val screenWidth: Int,
    val screenHeight: Int,
    val pixelStep: Int,
    val stats: RenderStats,
)

/** Draws a prepared fractal into a bitmap using all CPU cores. Cancels promptly when the view changes. */
class FractalRenderer {

    suspend fun render(
        fractal: Fractal,
        plan: PerturbationPlan,
        viewport: Viewport,
        screenWidth: Int,
        screenHeight: Int,
        pixelStep: Int,
        palette: ColorPalette,
    ): RenderedFrame = withContext(Dispatchers.Default) {
        val w = (screenWidth + pixelStep - 1) / pixelStep
        val h = (screenHeight + pixelStep - 1) / pixelStep
        val pixels = IntArray(w * h)
        val scale = viewport.unitsPerPixel(screenWidth, screenHeight)
        val half = pixelStep / 2.0
        val bands = Runtime.getRuntime().availableProcessors() * 4

        val bandStats = coroutineScope {
            (0 until bands).map { band ->
                async {
                    var maxEscape = 0
                    var inside = 0
                    var row = band
                    while (row < h) {
                        ensureActive()
                        val dIm = -(row * pixelStep + half - screenHeight / 2.0) * scale
                        val offset = row * w
                        for (col in 0 until w) {
                            val dRe = (col * pixelStep + half - screenWidth / 2.0) * scale
                            val value = plan.escapeValue(dRe, dIm)
                            if (value < 0) inside++ else if (value > maxEscape) maxEscape = value.toInt()
                            pixels[offset + col] = palette.colorFor(value)
                        }
                        row += bands
                    }
                    RenderStats(maxEscape, inside)
                }
            }.awaitAll()
        }
        val stats = RenderStats(bandStats.maxOf { it.maxEscape }, bandStats.sumOf { it.insideCount })

        val bitmap = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        RenderedFrame(fractal, bitmap.asImageBitmap(), viewport, screenWidth, screenHeight, pixelStep, stats)
    }
}
