package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quadlogixs.debugtool.ui.components.AppSwitch
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.ItemWithSwitch
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.showDebugToast
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider
import com.quadlogixs.debugtool.ui.components.isValidJson
import com.quadlogixs.debugtool.core.MockApiResponseStore
import com.quadlogixs.debugtool.core.SavedMockResponse
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme

@Composable
fun MockApiResponsesDialog(onDismiss: () -> Unit) {
    DebugToolTheme {
        MockApiResponsesDialogBody(onDismiss = onDismiss)
    }
}

@Composable
private fun MockApiResponsesDialogBody(onDismiss: () -> Unit) {
    val mocks by MockApiResponseStore.mocks.collectAsState()
    val globalEnabled by MockApiResponseStore.globalEnabledFlow.collectAsState()
    var editingMock by remember { mutableStateOf<SavedMockResponse?>(null) }

    if (editingMock != null) {
        EditMockResponseDialog(
            mock = editingMock!!,
            onDismiss = { editingMock = null },
            onSave = { updated ->
                MockApiResponseStore.update(updated)
                editingMock = null
            },
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = DebugColors.Background,
            borderColor = DebugColors.Border,
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TitleMediumText(
                            text = "Response Mocker",
                            overrideColor = DebugColors.AccentGreen,
                        )
                        SpacerWeight(1f)
                        ResourceImage(
                            image = R.drawable.ic_close_receipt,
                            modifier = Modifier
                                .size(18.sdp)
                                .safeClickable { onDismiss() },
                        )
                    }
                    SpaceDefault()
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpacerHeight(5.sdp)
                    ItemWithSwitch(
                        title = "Enable saved mocks",
                        icon = R.drawable.ic_api_halt,
                        isChecked = globalEnabled,
                        onCheckedChange = { MockApiResponseStore.setGlobalEnabled(it) },
                    )
                    SpacerHeight(8.sdp)
                    if (mocks.isEmpty()) {
                        BodySmallText(
                            text = "No saved mocks yet. Use Halt API → Save as Mock on a response.",
                            modifier = Modifier.padding(vertical = 8.sdp),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 360.sdp)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            mocks.forEachIndexed { index, mock ->
                                MockResponseListItem(
                                    mock = mock,
                                    onEdit = { editingMock = mock },
                                    onToggle = { enabled ->
                                        MockApiResponseStore.setEnabled(mock.id, enabled)
                                    },
                                    onDelete = { MockApiResponseStore.remove(mock.id) },
                                )
                                if (index != mocks.lastIndex) {
                                    HorizontalLineDivider()
                                    SpacerHeight(4.sdp)
                                }
                            }
                        }
                        SpacerHeight(8.sdp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
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
                                        text = "Clear All",
                                        fontSize = 12.textSdp,
                                        overrideColor = MaterialTheme.colorScheme.onTertiary,
                                    )
                                },
                                onClick = { MockApiResponseStore.clearAll() },
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun EditMockResponseDialog(
    mock: SavedMockResponse,
    onDismiss: () -> Unit,
    onSave: (SavedMockResponse) -> Unit,
) {
    val context = LocalContext.current
    var label by remember(mock.id) { mutableStateOf(mock.label) }
    var urlPattern by remember(mock.id) { mutableStateOf(mock.urlPattern) }
    var httpMethod by remember(mock.id) { mutableStateOf(mock.httpMethod) }
    var statusCodeText by remember(mock.id) { mutableStateOf(mock.statusCode.toString()) }
    var responseBody by remember(mock.id) { mutableStateOf(mock.responseBody) }
    var simulateTimeout by remember(mock.id) { mutableStateOf(mock.simulateTimeout) }

    Dialog(onDismissRequest = onDismiss) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TitleMediumText(
                            text = "Edit Mock Response",
                            overrideColor = MaterialTheme.colorScheme.onPrimary,
                        )
                        SpacerWeight(1f)
                        ResourceImage(
                            image = R.drawable.ic_close_receipt,
                            modifier = Modifier
                                .size(18.sdp)
                                .safeClickable { onDismiss() },
                        )
                    }
                    SpaceDefault()
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    Column(
                        modifier = Modifier
                            .heightIn(max = 420.sdp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        SpacerHeight(5.sdp)
                        TextInputFieldApp(
                            modifier = Modifier.fillMaxWidth(),
                            value = label,
                            onValueChange = { label = it },
                            fieldLabel = "Label",
                            placeholder = "Optional label",
                            singleLine = true,
                        )
                        SpacerHeight(8.sdp)
                        TextInputFieldApp(
                            modifier = Modifier.fillMaxWidth(),
                            value = urlPattern,
                            onValueChange = { urlPattern = it },
                            fieldLabel = "URL Pattern",
                            placeholder = "Full URL or path suffix",
                            singleLine = false,
                            maxLines = 3,
                            heightField = 70.sdp,
                        )
                        SpacerHeight(8.sdp)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextInputFieldApp(
                                modifier = Modifier.weight(1f),
                                value = httpMethod,
                                onValueChange = { httpMethod = it.uppercase() },
                                fieldLabel = "Method",
                                placeholder = "POST",
                                singleLine = true,
                            )
                            SpacerWidth(8.sdp)
                            TextInputFieldApp(
                                modifier = Modifier.weight(1f),
                                value = statusCodeText,
                                onValueChange = { statusCodeText = it.filter(Char::isDigit) },
                                fieldLabel = "Status Code",
                                placeholder = "200",
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }
                        SpacerHeight(8.sdp)
                        TextInputFieldApp(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.sdp),
                            value = responseBody,
                            onValueChange = { responseBody = it },
                            fieldLabel = "Response JSON",
                            placeholder = "{}",
                            singleLine = false,
                            maxLines = Int.MAX_VALUE,
                            heightField = 220.sdp,
                        )
                        SpacerHeight(8.sdp)
                        ItemWithSwitch(
                            title = "Simulate timeout",
                            icon = R.drawable.ic_api_halt,
                            isChecked = simulateTimeout,
                            onCheckedChange = { simulateTimeout = it },
                        )
                    }
                    SpaceDefault()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
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
                                    text = "Cancel",
                                    fontSize = 12.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.onTertiary,
                                )
                            },
                            onClick = onDismiss,
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
                                    text = "Save",
                                    fontSize = 12.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.background,
                                )
                            },
                            onClick = {
                                val statusCode = statusCodeText.toIntOrNull()
                                when {
                                    urlPattern.isBlank() -> {
                                        context.showDebugToast("URL pattern is required")
                                    }
                                    httpMethod.isBlank() -> {
                                        context.showDebugToast("HTTP method is required")
                                    }
                                    statusCode == null || statusCode !in 100..599 -> {
                                        context.showDebugToast("Enter a valid status code (100–599)")
                                    }
                                    !responseBody.isValidJson() -> {
                                        context.showDebugToast("Response must be valid JSON")
                                    }
                                    else -> {
                                        onSave(
                                            mock.copy(
                                                label = label.trim(),
                                                urlPattern = urlPattern.trim(),
                                                httpMethod = httpMethod.trim(),
                                                statusCode = statusCode,
                                                responseBody = responseBody.trim(),
                                                simulateTimeout = simulateTimeout,
                                            ),
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun MockResponseListItem(
    mock: SavedMockResponse,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .safeClickable { onEdit() },
            ) {
                LabelMediumText(
                    text = mock.label.ifBlank { "Mock ${mock.statusCode}" },
                    fontSize = 12.textSdp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                SpacerHeight(2.sdp)
                LabelSmallText(
                    text = "${mock.httpMethod} · HTTP ${mock.statusCode}${if (mock.simulateTimeout) " · Timeout" else ""}",
                    fontSize = 10.textSdp,
                    overrideColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SpacerHeight(2.sdp)
                BodySmallText(
                    text = mock.urlPattern,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SpacerWidth(4.sdp)
            ResourceImage(
                image = com.quadlogixs.debugtool.R.drawable.ic_edit,
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable { onEdit() },
            )
            SpacerWidth(4.sdp)
            AppSwitch(
                modifier = Modifier
                    .height(15.sdp)
                    .width(25.sdp)
                    .scale(0.6f),
                isChecked = mock.enabled,
                onCheckedChange = onToggle,
            )
            SpacerWidth(4.sdp)
            ResourceImage(
                image = R.drawable.ic_delete,
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable { onDelete() },
            )
        }
    }
}
