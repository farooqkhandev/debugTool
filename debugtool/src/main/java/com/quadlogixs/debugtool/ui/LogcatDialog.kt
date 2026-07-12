package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme

@Composable
fun LogcatDialog(
    viewModel: LogcatViewModel,
    onDismiss: () -> Unit,
) {
    DebugToolTheme {
        LogcatDialogContent(viewModel = viewModel, onDismiss = onDismiss)
    }
}

@Composable
private fun LogcatDialogContent(
    viewModel: LogcatViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isPaused by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CardContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            containerColor = DebugColors.Background,
            borderColor = DebugColors.Border,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.sdp)
                    .fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TitleMediumText(
                        text = "Local Logs",
                        overrideColor = DebugColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    SpacerWeight(1f)
                    ResourceImage(
                        image = R.drawable.ic_close_receipt,
                        modifier = Modifier
                            .size(18.sdp)
                            .safeClickable { onDismiss() },
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(DebugColors.TextSecondary),
                    )
                }

                SpaceDefault()

                LogcatSearchBar(viewModel = viewModel)

                Box(
                    modifier = Modifier
                        .height(400.sdp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, DebugColors.Border, RoundedCornerShape(8.dp)),
                ) {
                    LogcatScreen(viewModel = viewModel)
                }

                SpaceDefault()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPaused) DebugColors.Warning else DebugColors.Success,
                            ),
                    )
                    SpacerWidth(6.sdp)
                    LabelSmallText(
                        text = if (isPaused) "Paused" else "Live",
                        overrideColor = if (isPaused) DebugColors.Warning else DebugColors.Success,
                    )
                    SpacerWeight(1f)
                    BaseButton(
                        modifier = Modifier.height(34.dp),
                        primary = false,
                        containerColor = DebugColors.SurfaceElevated,
                        onClick = {
                            isPaused = !isPaused
                            // Pause is visual for now; streaming still controlled by VM lifecycle.
                        },
                    ) {
                        LabelMediumText(
                            text = if (isPaused) "Resume" else "Pause",
                            fontSize = 12.textSdp,
                            overrideColor = DebugColors.TextPrimary,
                        )
                    }
                    SpacerWidth(8.sdp)
                    BaseButton(
                        modifier = Modifier.height(34.dp),
                        primary = false,
                        containerColor = DebugColors.SurfaceElevated,
                        borderColor = DebugColors.AccentRed,
                        onClick = {
                            val textToCopy = viewModel.logs.value.joinToString("\n") {
                                "[${it.level}] ${it.tag}: ${it.message}"
                            }
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            context.showDebugToast("Logs copied to clipboard")
                        },
                    ) {
                        LabelMediumText(
                            text = "Copy",
                            fontSize = 12.textSdp,
                            overrideColor = DebugColors.TextPrimary,
                        )
                    }
                    SpacerWidth(8.sdp)
                    BaseButton(
                        modifier = Modifier.height(34.dp),
                        containerColor = DebugColors.AccentTeal,
                        onClick = { viewModel.clearLogs() },
                    ) {
                        LabelMediumText(
                            text = "Clear",
                            fontSize = 12.textSdp,
                            overrideColor = DebugColors.OnAccent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogcatSearchBar(viewModel: LogcatViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextInputFieldApp(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp),
            value = viewModel.query,
            onValueChange = { viewModel.query = it },
            fieldLabel = "",
            placeholder = "Search logs...",
            labelTextSize = 12.textSdp,
        )

        SpacerWidth(8.sdp)

        LabelSmallText(
            text = if (viewModel.useRegex) "Regex On" else "Regex Off",
            overrideColor = if (viewModel.useRegex) DebugColors.AccentTeal else DebugColors.TextSecondary,
            modifier = Modifier.safeClickable { viewModel.useRegex = !viewModel.useRegex },
        )
    }
}
