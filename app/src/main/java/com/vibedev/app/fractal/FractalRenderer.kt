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
)

/** Draws a fractal into a bitmap using all CPU cores. Cancels promptly when the view changes. */
class FractalRenderer(private val palette: ColorPalette) {

    suspend fun render(
        fractal: Fractal,
        viewport: Viewport,
        screenWidth: Int,
        screenHeight: Int,
        pixelStep: Int,
    ): RenderedFrame = withContext(Dispatchers.Default) {
        val w = (screenWidth + pixelStep - 1) / pixelStep
        val h = (screenHeight + pixelStep - 1) / pixelStep
        val pixels = IntArray(w * h)
        val maxIter = viewport.iterationBudget()
        val half = pixelStep / 2.0
        val bands = Runtime.getRuntime().availableProcessors() * 4

        coroutineScope {
            (0 until bands).map { band ->
                async {
                    var row = band
                    while (row < h) {
                        ensureActive()
                        val im = viewport.toIm(row * pixelStep + half, screenWidth, screenHeight)
                        val offset = row * w
                        for (col in 0 until w) {
                            val re = viewport.toRe(col * pixelStep + half, screenWidth, screenHeight)
                            pixels[offset + col] = palette.colorFor(fractal.escapeValue(re, im, maxIter))
                        }
                        row += bands
                    }
                }
            }.awaitAll()
        }

        val bitmap = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        RenderedFrame(fractal, bitmap.asImageBitmap(), viewport, screenWidth, screenHeight, pixelStep)
    }
}
