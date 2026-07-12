package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.showDebugToast
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider
import com.quadlogixs.debugtool.core.clearCrashLogs
import com.quadlogixs.debugtool.core.getCrashLogs
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.components.NoRecentFoundItem
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme

@Composable
fun CrashLogViewer(onDismissRequest: () -> Unit) {
    DebugToolTheme {
        CrashLogViewerBody(onDismissRequest = onDismissRequest)
    }
}

@Composable
private fun CrashLogViewerBody(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var crashLogs by remember { mutableStateOf(getCrashLogs(context).takeIf { it.length>50 }?.split("CRASH_ENDS")?: listOf()) }
    Dialog(onDismissRequest = onDismissRequest) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = DebugColors.Background,
            borderColor = DebugColors.Border,
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .height(400.sdp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    SpacerHeight(5.sdp)
                    Row {
                        TitleMediumText(
                            text = "Crash Reports",
                            overrideColor = DebugColors.AccentOrange
                        )
                        SpacerWeight(1f)
                        if (crashLogs.isNotEmpty()) {
                            ResourceImage(
                                image = R.drawable.ic_clear,
                                modifier = Modifier
                                    .size(18.sdp)
                                    .safeClickable {
                                        clearCrashLogs(context)
                                        crashLogs = listOf()
                                    })
                            SpacerWidth(10.sdp)
                        }
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
                    if (crashLogs.isNotEmpty()) {
                        crashLogs.forEach {
                            if (it.length>50) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    TextInputFieldApp(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.sdp)
                                            .padding(top = 7.dp),
                                        value = it.substringAfter("|||"),
                                        singleLine = false,
                                        readOnly = true,
                                        labelTextSize=14.textSdp,
                                        heightField = 250.sdp,
                                        maxLines = Int.MAX_VALUE,
                                        onValueChange = {},
                                        fieldLabel = "Stack Trace: ${it.substringBefore("|||").trim()}",
                                        placeholder = "Stack Trace",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                                    )
                                    Column {
                                        SpacerHeight(30.sdp)
                                        ResourceImage(
                                            image = com.quadlogixs.debugtool.R.drawable.ic_copy,
                                            modifier = Modifier
                                                .size(18.sdp)
                                                .safeClickable {
                                                    clipboardManager.setText(
                                                        AnnotatedString(
                                                            it
                                                        )
                                                    )
                                                    context.showDebugToast("Details copied to clipboard")
                                                })

                                    }
                                }
                            }
                        }

                    } else {
                        SpacerHeight(20.sdp)
                        NoRecentFoundItem(
                            title = "You Don't have any crash logs",
                            icon = R.drawable.ic_no_history
                        )
                    }
                }

            })
    }
}