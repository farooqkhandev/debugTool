package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.theme.LocalExtraThemeColors
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider
// removed LocalExtraThemeColors
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.components.NoRecentFoundItem
import com.quadlogixs.debugtool.ui.MemoryTracker
import kotlin.math.log10

@Composable
fun MemoryUsageViewer(
    onDismissRequest: () -> Unit,
    routeTrail: String = "debug",
) {
    // Snapshot after logging so the dialog shows the fresh entry.
    var logs by remember { mutableStateOf(MemoryTracker.logs) }
    LaunchedEffect(Unit) {
        MemoryTracker.logMemory(routeTrail)
        logs = MemoryTracker.logs
    }

    Dialog(onDismissRequest = onDismissRequest) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .height(350.sdp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    SpacerHeight(5.sdp)
                    Row {
                        TitleMediumText(
                            text = "Memory Usage",
                            overrideColor = MaterialTheme.colorScheme.onPrimary
                        )
                        SpacerWeight(1f)
                        ResourceImage(
                            image = com.quadlogixs.debugtool.R.drawable.ic_close_receipt,
                            modifier = Modifier
                                .size(18.sdp)
                                .safeClickable {
                                    onDismissRequest()
                                })
                    }
                    SpaceDefault()
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpaceDefault()
                    if (logs.isNotEmpty()) {
                        logs.forEach { item ->
                            Row {
                                LabelMediumText(
                                    text = "Screen: ",
                                    overrideColor = MaterialTheme.colorScheme.onPrimary
                                )
                                LabelSmallText(text = item.screen)
                            }
                            SpacerHeight(2.sdp)
                            Row(verticalAlignment = Alignment.Bottom) {
                                LabelMediumText(
                                    text = "Memory: ",
                                    overrideColor = MaterialTheme.colorScheme.onPrimary
                                )
                                LabelSmallText(text = "${item.usedMemory.toReadableMemorySize()} / ${item.totalMemory.toReadableMemorySize()}")
                                SpacerWidth(10.sdp)
                                BodySmallText(
                                    text = "Time: ${item.dateTime}",
                                    fontSize = 10.textSdp,
                                    overrideColor = LocalExtraThemeColors.current.onDisable
                                )
                            }
                            HorizontalLineDivider(horizontalPadding = 0.dp)
                            SpaceDefault()
                        }
                    } else {
                        SpacerHeight(20.sdp)
                        NoRecentFoundItem(
                            title = "You Don't have any memory stats",
                            icon = R.drawable.ic_no_history
                        )
                    }
                }

            })
    }
}

fun Long.toReadableMemorySize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return String.format(
        "%.2f %s",
        this / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}
