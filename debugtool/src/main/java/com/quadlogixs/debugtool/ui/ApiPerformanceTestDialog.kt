package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
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
import com.quadlogixs.debugtool.R


@Composable
fun ApiPerformanceTestDialog(
    onDismiss: () -> Unit,
    viewModel: ApiPerformanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val records by viewModel.records.collectAsStateWithLifecycle()

    var manualUrl by remember { mutableStateOf("") }
    var showManualTest by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .fillMaxWidth()
                ) {
                    // Header Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TitleMediumText(
                            text = "API Performance Test",
                            overrideColor = MaterialTheme.colorScheme.onPrimary
                        )
                        SpacerWeight(1f)
                        ResourceImage(
                            image = R.drawable.ic_close_receipt,
                            modifier = Modifier
                                .size(18.sdp)
                                .safeClickable { onDismiss() }
                        )
                    }

                    SpaceDefault()
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpacerHeight(5.sdp)

                    // Records List
                    if (records.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BodySmallText(text = "No API calls recorded yet.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.sdp)
                        ) {
                            items(records.reversed()) { record ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            record.duration < 1500 -> Color(0xFFB2FF59)
                                            record.duration < 2000 -> Color(0xFFFFF176)
                                            else -> Color(0xFFFF8A80)
                                        }
                                    )
                                ) {
                                    Column(Modifier.padding(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            BodySmallText(
                                                text = "${record.method} • ${record.statusCode}",
                                                fontSize = 12.textSdp
                                            )
                                            SpacerWeight(1f)
                                            ResourceImage(
                                                image = R.drawable.ic_copy,
                                                modifier = Modifier
                                                    .size(16.sdp)
                                                    .safeClickable {
                                                        clipboardManager.setText(
                                                            AnnotatedString(record.url)
                                                        )
                                                        context.showDebugToast("Copied URL")
                                                    }
                                            )
                                        }
                                        LabelSmallText(
                                            text = record.url,
                                            fontSize = 11.textSdp,
                                            overrideColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        LabelSmallText(
                                            text = "Response Time: ${record.duration} ms",
                                            fontSize = 11.textSdp,
                                            overrideColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SpacerHeight(10.sdp)
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpacerHeight(10.sdp)

                    // Manual Test Section
                    if (showManualTest) {
                        TextInputFieldApp(
                            modifier = Modifier.fillMaxWidth(),
                            value = manualUrl,
                            fieldLabel = "Manual Test URL",
                            placeholder = "Enter API URL",
                            onValueChange = { manualUrl = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )

                        SpacerHeight(8.sdp)

                        BaseButton(
                            modifier = Modifier.height(35.dp),
                            primary = true,
                            enabled = manualUrl.isNotBlank() && !isTesting,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            onClick = {
                                isTesting = true
                                viewModel.testApiSpeed(manualUrl) { result ->
                                    isTesting = false
                                    testResult = result
                                }
                            }
                        ) {
                            LabelMediumText(
                                text = if (isTesting) "Testing..." else "Test API Speed",
                                fontSize = 12.textSdp,
                                overrideColor = MaterialTheme.colorScheme.background
                            )
                        }

                        testResult?.let {
                            SpacerHeight(8.sdp)
                            BodySmallText(
                                text = it,
                                fontSize = 11.textSdp,
                                overrideColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        SpacerHeight(10.sdp)
                    }

                    // Bottom Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
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
                                    text = "Manual",
                                    fontSize = 12.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.onTertiary
                                )
                            },
                            onClick = { showManualTest = !showManualTest }
                        )

                        SpacerWidth(10.sdp)
                        BaseButton(
                            modifier = Modifier.height(30.dp),
                            primary = false,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            borderColor = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small,
                            enabled = true,
                            content = {
                                LabelMediumText(
                                    text = "Clear All",
                                    fontSize = 12.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.onTertiary
                                )
                            },
                            onClick = { viewModel.clear() }
                        )

                        SpacerWidth(10.sdp)
                        BaseButton(
                            modifier = Modifier.height(30.dp),
                            primary = true,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            borderColor = Color.Unspecified,
                            shape = MaterialTheme.shapes.small,
                            enabled = true,
                            content = {
                                LabelMediumText(
                                    text = "Close",
                                    fontSize = 12.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.background
                                )
                            },
                            onClick = { onDismiss() }
                        )
                    }
                }
            }
        )
    }
}
