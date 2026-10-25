package com.vibedev.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.vibedev.app.fractal.Fractal
import com.vibedev.app.fractal.JuliaFractal
import com.vibedev.app.fractal.MandelbrotFractal
import com.vibedev.app.fractal.Viewport
import java.math.BigDecimal

enum class FractalMode { MANDELBROT, JULIA }

/** What the explorer is showing. Each mode remembers its own view. */
class ExplorerState(
    mode: FractalMode = FractalMode.MANDELBROT,
    mandelbrotView: Viewport = Viewport.MANDELBROT_HOME,
    juliaView: Viewport = Viewport.JULIA_HOME,
) {
    var mode by mutableStateOf(mode)
        private set
    var mandelbrotView by mutableStateOf(mandelbrotView)
        private set
    var juliaView by mutableStateOf(juliaView)
        private set

    val viewport: Viewport
        get() = if (mode == FractalMode.MANDELBROT) mandelbrotView else juliaView

    /** The Julia set uses the center of the Mandelbrot view as its constant. */
    val juliaFractal: JuliaFractal
        get() = JuliaFractal(mandelbrotView.centerRe, mandelbrotView.centerIm)

    val fractal: Fractal
        get() = if (mode == FractalMode.MANDELBROT) MandelbrotFractal else juliaFractal

    fun transform(pan: Float, panY: Float, zoom: Float, fx: Float, fy: Float, w: Int, h: Int) {
        val updated = viewport.panned(pan, panY, w, h).zoomed(zoom, fx, fy, w, h)
        if (mode == FractalMode.MANDELBROT) mandelbrotView = updated else juliaView = updated
    }

    fun toggleMode() {
        mode = if (mode == FractalMode.MANDELBROT) {
            juliaView = Viewport.JULIA_HOME
            FractalMode.JULIA
        } else {
            FractalMode.MANDELBROT
        }
    }

    fun resetView() {
        if (mode == FractalMode.MANDELBROT) mandelbrotView = Viewport.MANDELBROT_HOME
        else juliaView = Viewport.JULIA_HOME
    }

    companion object {
        val Saver = listSaver<ExplorerState, Any>(
            save = {
                listOf(
                    it.mode.ordinal,
                    it.mandelbrotView.centerRe.toString(), it.mandelbrotView.centerIm.toString(), it.mandelbrotView.span,
                    it.juliaView.centerRe.toString(), it.juliaView.centerIm.toString(), it.juliaView.span,
                )
            },
            restore = {
                ExplorerState(
                    FractalMode.entries[it[0] as Int],
                    Viewport(BigDecimal(it[1] as String), BigDecimal(it[2] as String), it[3] as Double),
                    Viewport(BigDecimal(it[4] as String), BigDecimal(it[5] as String), it[6] as Double),
                )
            },
        )
    }
}

@Composable
fun rememberExplorerState(): ExplorerState = rememberSaveable(saver = ExplorerState.Saver) { ExplorerState() }
