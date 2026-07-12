package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.NoRecentFoundItem
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme
import kotlin.math.log10
import kotlinx.coroutines.delay

@Composable
fun MemoryUsageViewer(onDismissRequest: () -> Unit) {
    DebugToolTheme {
        MemoryUsageViewerContent(onDismissRequest = onDismissRequest)
    }
}

@Composable
private fun MemoryUsageViewerContent(onDismissRequest: () -> Unit) {
    var usedBytes by remember { mutableStateOf(0L) }
    var totalBytes by remember { mutableStateOf(0L) }
    var maxBytes by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val runtime = Runtime.getRuntime()
            usedBytes = runtime.totalMemory() - runtime.freeMemory()
            totalBytes = runtime.totalMemory()
            maxBytes = runtime.maxMemory()
            delay(1000)
        }
    }

    val freeBytes = (totalBytes - usedBytes).coerceAtLeast(0L)
    val ratio = if (maxBytes > 0) (usedBytes.toFloat() / maxBytes.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (ratio * 100f).toInt()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CardContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            containerColor = DebugColors.Background,
            borderColor = DebugColors.Border,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.sdp)
                    .height(480.sdp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TitleMediumText(
                        text = "Memory Usage",
                        overrideColor = DebugColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    SpacerWeight(1f)
                    LabelSmallText(
                        text = "• Live",
                        overrideColor = DebugColors.AccentPurple,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ResourceImage(
                        image = R.drawable.ic_close_receipt,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(18.sdp)
                            .safeClickable { onDismissRequest() },
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(DebugColors.TextSecondary),
                    )
                }

                SpacerHeight(12.sdp)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DebugColors.Surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DebugColors.Border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            MemoryStatRow("Used", usedBytes.toReadableMemorySize())
                            SpacerHeight(8.sdp)
                            MemoryStatRow("Free", freeBytes.toReadableMemorySize())
                            SpacerHeight(8.sdp)
                            MemoryStatRow("Max", maxBytes.toReadableMemorySize())
                        }
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.size(110.dp)) {
                                val stroke = 14.dp.toPx()
                                drawArc(
                                    color = DebugColors.Border,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                                    size = Size(size.minDimension, size.minDimension),
                                    topLeft = Offset.Zero,
                                )
                                drawArc(
                                    color = DebugColors.AccentPurple,
                                    startAngle = -90f,
                                    sweepAngle = 360f * ratio,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                                    size = Size(size.minDimension, size.minDimension),
                                    topLeft = Offset.Zero,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                TitleMediumText(
                                    text = "$percent%",
                                    overrideColor = DebugColors.AccentPurple,
                                    fontWeight = FontWeight.Bold,
                                )
                                LabelSmallText(
                                    text = "${usedBytes.toReadableMemorySize()} / ${maxBytes.toReadableMemorySize()}",
                                    overrideColor = DebugColors.TextSecondary,
                                    fontSize = 9.textSdp,
                                )
                            }
                        }
                    }
                }

                SpacerHeight(10.sdp)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DebugColors.Surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DebugColors.Border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LabelMediumText(
                                text = "Allocation Rate",
                                overrideColor = DebugColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            SpacerWeight(1f)
                            LabelSmallText(
                                text = "Live heap sample",
                                overrideColor = DebugColors.AccentPurple,
                            )
                        }
                        SpacerHeight(10.sdp)
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = DebugColors.AccentPurple,
                            trackColor = DebugColors.Border,
                        )
                    }
                }

                SpacerHeight(10.sdp)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DebugColors.Surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DebugColors.Border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LabelMediumText(
                            text = "Heap Details",
                            overrideColor = DebugColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        SpacerHeight(8.sdp)
                        if (MemoryTracker.logs.isNotEmpty()) {
                            MemoryTracker.logs.take(8).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        LabelSmallText(
                                            text = item.screen,
                                            overrideColor = DebugColors.TextPrimary,
                                            maxLines = 1,
                                        )
                                        BodySmallText(
                                            text = item.dateTime,
                                            overrideColor = DebugColors.TextSecondary,
                                            fontSize = 10.textSdp,
                                        )
                                    }
                                    LabelSmallText(
                                        text = "${item.usedMemory.toReadableMemorySize()} / ${item.totalMemory.toReadableMemorySize()}",
                                        overrideColor = DebugColors.AccentPurple,
                                    )
                                }
                            }
                        } else {
                            NoRecentFoundItem(
                                title = "No screen heap samples yet",
                                icon = R.drawable.ic_no_history,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryStatRow(label: String, value: String) {
    Column {
        BodySmallText(text = label, overrideColor = DebugColors.TextSecondary)
        LabelMediumText(
            text = value,
            overrideColor = DebugColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

fun Long.toReadableMemorySize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return String.format(
        "%.2f %s",
        this / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups],
    )
}
