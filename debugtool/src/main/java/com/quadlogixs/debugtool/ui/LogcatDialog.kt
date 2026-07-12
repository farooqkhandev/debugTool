package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.showDebugToast
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider

@Composable
fun LogcatDialog(
    viewModel: LogcatViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        CardContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.sdp)
        ) {
            Column(
                modifier = Modifier
                    .padding(10.sdp)
                    .fillMaxWidth()
            ) {
                // 🔹 Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TitleMediumText(
                        text = "LogCat Viewer",
                        overrideColor = MaterialTheme.colorScheme.onPrimary
                    )
                    SpacerWeight(1f)
                    ResourceImage(
                        image = com.quadlogixs.debugtool.R.drawable.ic_close_receipt,
                        modifier = Modifier
                            .size(18.sdp)
                            .safeClickable { onDismiss() }
                    )
                }

                SpaceDefault()
                HorizontalLineDivider(horizontalPadding = 0.dp)
                SpaceDefault()

                // 🔹 Search and filters
                LogcatSearchBar(viewModel = viewModel)

                // 🔹 Log list
                Box(
                    modifier = Modifier
                        .height(400.sdp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LogcatScreen(viewModel = viewModel)
                }

                SpaceDefault()

                // 🔹 Bottom buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    BaseButton(
                        modifier = Modifier.height(30.dp),
                        primary = false,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        borderColor = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small,
                        enabled = true,
                        content = {
                            LabelMediumText(
                                text = "Copy All",
                                fontSize = 12.textSdp,
                                overrideColor = MaterialTheme.colorScheme.onTertiary
                            )
                        },
                        onClick = {
                            val textToCopy = viewModel.logs.value.joinToString("\n") {
                                "[${it.level}] ${it.tag}: ${it.message}"
                            }
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            context.showDebugToast("Logs copied to clipboard")
                        }
                    )

                    SpacerWidth(10.sdp)

                    BaseButton(
                        modifier = Modifier.height(30.dp),
                        primary = true,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small,
                        enabled = true,
                        content = {
                            LabelMediumText(
                                text = "Clear Logs",
                                fontSize = 12.textSdp,
                                overrideColor = MaterialTheme.colorScheme.background
                            )
                        },
                        onClick = { viewModel.clearLogs() }
                    )
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
            fieldLabel = "Search logs",
            placeholder = "Search by tag, text, PID",
            labelTextSize = 12.textSdp
        )

        ResourceImage(
            image = com.quadlogixs.debugtool.R.drawable.ic_copy,
            modifier = Modifier
                .size(18.sdp)
                .padding(start = 8.dp)
                .safeClickable {
                    viewModel.useRegex = !viewModel.useRegex
                }
        )

        SpacerWidth(5.sdp)

        LabelSmallText(
            text = if (viewModel.useRegex) "Regex On" else "Regex Off",
            overrideColor = MaterialTheme.colorScheme.onTertiary
        )
    }
}
