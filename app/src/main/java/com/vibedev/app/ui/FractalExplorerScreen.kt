package com.vibedev.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.vibedev.app.fractal.CosinePalette
import com.vibedev.app.fractal.FractalRenderer
import com.vibedev.app.fractal.RenderedFrame
import com.vibedev.app.fractal.Viewport
import java.util.Locale
import kotlin.math.roundToInt

// Coarse first so something shows quickly, then full detail.
private val RENDER_PASSES = listOf(6, 2, 1)

@Composable
fun FractalExplorerScreen(
    state: ExplorerState = rememberExplorerState(),
    renderer: FractalRenderer = remember { FractalRenderer(CosinePalette()) },
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var frame by remember { mutableStateOf<RenderedFrame?>(null) }

    val fractal = state.fractal
    val viewport = state.viewport
    LaunchedEffect(fractal, viewport, size) {
        if (frame?.fractal != fractal) frame = null // don't stretch a different fractal's picture
        if (size.width == 0 || size.height == 0) return@LaunchedEffect
        for (step in RENDER_PASSES) {
            frame = renderer.render(fractal, viewport, size.width, size.height, step)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onSizeChanged { size = it }
                .pointerInput(state) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val w = this.size.width
                        val h = this.size.height
                        state.transform(pan.x, pan.y, zoom, centroid.x, centroid.y, w, h)
                    }
                },
        ) {
            frame?.let { drawFrame(it, viewport) }
            if (state.mode == FractalMode.MANDELBROT) drawCrosshair()
        }

        InfoLabel(state, Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(8.dp))

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
            FilledTonalButton(onClick = state::resetView) { Text("Reset") }
            Button(onClick = state::toggleMode) {
                Text(if (state.mode == FractalMode.MANDELBROT) "Show Julia set" else "Back to Mandelbrot")
            }
        }
    }
}

/** Draws the last finished picture, shifted and scaled to match the current view while gestures run. */
private fun DrawScope.drawFrame(frame: RenderedFrame, current: Viewport) {
    val w = size.width.toInt()
    val h = size.height.toInt()
    if (frame.screenWidth != w || frame.screenHeight != h) return
    val old = frame.viewport
    val curScale = current.unitsPerPixel(w, h)
    val ratio = old.unitsPerPixel(w, h) / curScale
    val cornerRe = old.toRe(0.0, w, h)
    val cornerIm = old.toIm(0.0, w, h)
    val x = (cornerRe - current.centerRe) / curScale + w / 2.0
    val y = -(cornerIm - current.centerIm) / curScale + h / 2.0
    val coveredW = frame.image.width * frame.pixelStep
    val coveredH = frame.image.height * frame.pixelStep
    drawImage(
        image = frame.image,
        dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
        dstSize = IntSize((coveredW * ratio).roundToInt(), (coveredH * ratio).roundToInt()),
    )
}

private fun DrawScope.drawCrosshair() {
    val c = center
    val arm = 14.dp.toPx()
    val stroke = 2.dp.toPx()
    listOf(Color.Black to stroke * 2, Color.White to stroke).forEach { (color, width) ->
        drawLine(color, Offset(c.x - arm, c.y), Offset(c.x + arm, c.y), width)
        drawLine(color, Offset(c.x, c.y - arm), Offset(c.x, c.y + arm), width)
    }
}

@Composable
private fun InfoLabel(state: ExplorerState, modifier: Modifier = Modifier) {
    val v = state.mandelbrotView
    val title = if (state.mode == FractalMode.MANDELBROT) "Mandelbrot set" else "Julia set"
    val zoom = Viewport.MANDELBROT_HOME.span / state.viewport.span
    Column(
        modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = Color.White)
        Text(
            String.format(Locale.US, "c = %.10f %+.10fi", v.centerRe, v.centerIm),
            color = Color.White.copy(alpha = 0.85f),
        )
        Text(String.format(Locale.US, "zoom ×%.3g", zoom), color = Color.White.copy(alpha = 0.7f))
    }
}
