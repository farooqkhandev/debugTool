package com.quadlogixs.debugtool.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.core.DebugUiToolsStore

private val SimulatorBackdropColor = Color(0xFF8E8E93)
private val GridLineColor = Color(0x66FF2D55)

@Composable
fun DebugScreenSizeSimulatorFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val preset by DebugUiToolsStore.screenSizePreset.collectAsState()
    if (preset == null) {
        content()
        return
    }

    val baseConfiguration = LocalConfiguration.current
    val simulatedConfiguration = remember(preset, baseConfiguration) {
        Configuration(baseConfiguration).apply {
            screenWidthDp = preset!!.widthDp
            screenHeightDp = preset!!.heightDp
            smallestScreenWidthDp = minOf(preset!!.widthDp, preset!!.heightDp)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SimulatorBackdropColor),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(preset!!.widthDp.dp)
                .height(preset!!.heightDp.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalConfiguration provides simulatedConfiguration,
            ) {
                content()
            }
        }

        LabelSmallText(
            text = preset!!.displayName,
            overrideColor = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
        )
    }
}

@Composable
fun DebugLayoutGridOverlay(modifier: Modifier = Modifier) {
    val enabled by DebugUiToolsStore.gridOverlayEnabled.collectAsState()
    if (!enabled) return

    val density = LocalDensity.current
    val gridStepPx = with(density) { 8.dp.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        var x = 0f
        while (x <= width) {
            drawLine(
                color = GridLineColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f,
            )
            x += gridStepPx
        }
        var y = 0f
        while (y <= height) {
            drawLine(
                color = GridLineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
            )
            y += gridStepPx
        }
    }
}
