package com.quadlogixs.debugtool.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quadlogixs.debugtool.ui.utils.arePermissionsGranted
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.utils.scanner.AnalyzerType
import com.quadlogixs.debugtool.ui.utils.scanner.CameraScreen
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.showDebugToast
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.utils.qrutils.QRTlvParseResult
import com.quadlogixs.debugtool.ui.utils.qrutils.QRTlvTableRow
import com.quadlogixs.debugtool.ui.utils.qrutils.checkCRCValidity
import com.quadlogixs.debugtool.ui.utils.qrutils.flattenEntriesToTableRows
import com.quadlogixs.debugtool.ui.utils.qrutils.formatParseResultSummary
import com.quadlogixs.debugtool.ui.utils.qrutils.parseQRTlvDebug

private enum class QrInputMode {
    Paste,
    Scan,
}

private object QrTlvTableLayout {
    val parentWidth = 52.dp
    val tagWidth = 40.dp
    val lengthWidth = 40.dp
}

@Composable
fun QrDebugScanDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val resultsScrollState = rememberScrollState()

    var manualInput by remember { mutableStateOf("") }
    var parseResult by remember { mutableStateOf<QRTlvParseResult?>(null) }
    var crcValid by remember { mutableStateOf(false) }
    var inputMode by remember { mutableStateOf(QrInputMode.Paste) }
    var scanLocked by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            context.arePermissionsGranted(permissions = listOf(Manifest.permission.CAMERA))
        )
    }

    fun copyFieldToClipboard() {
        if (manualInput.isBlank()) {
            context.showDebugToast("Nothing to copy")
        } else {
            clipboardManager.setText(AnnotatedString(manualInput))
            context.showDebugToast("Copied to clipboard")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                context.showDebugToast("Camera permission is required to scan QR codes")
            }
        }
    )

    fun processQrString(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return
        manualInput = trimmed
        parseResult = parseQRTlvDebug(trimmed)
        crcValid = checkCRCValidity(trimmed)
        scanLocked = true
    }

    fun onModeSelected(mode: QrInputMode) {
        inputMode = mode
        if (mode == QrInputMode.Scan) {
            scanLocked = false
            if (!hasCameraPermission) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TitleMediumText(
                            text = "QR Code Inspector",
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
                    SpacerHeight(8.sdp)

                    QrInputModeSelector(
                        selectedMode = inputMode,
                        onModeSelected = ::onModeSelected,
                    )

                    SpacerHeight(8.sdp)

                    when (inputMode) {
                        QrInputMode.Paste -> {
                            QrPasteInputWithCopyAction(
                                value = manualInput,
                                onValueChange = { manualInput = it },
                                onCopyClick = ::copyFieldToClipboard,
                            )
                            SpacerHeight(8.sdp)
                            BaseButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                primary = true,
                                containerColor = MaterialTheme.colorScheme.secondary,
                                onClick = { processQrString(manualInput) },
                            ) {
                                LabelMediumText(
                                    text = "Parse",
                                    fontSize = 12.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.background,
                                )
                            }
                        }

                        QrInputMode.Scan -> {
                            if (parseResult == null) {
                                if (!hasCameraPermission) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.sdp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        LabelSmallText(
                                            text = "Camera permission required for scanning",
                                            overrideColor = MaterialTheme.colorScheme.onPrimary,
                                        )
                                        SpacerHeight(5.sdp)
                                        BaseButton(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(38.dp),
                                            primary = true,
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                        ) {
                                            LabelMediumText(
                                                text = "Grant Permission",
                                                fontSize = 12.textSdp,
                                                overrideColor = MaterialTheme.colorScheme.background,
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.sdp)
                                            .clip(RoundedCornerShape(8.sdp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        CameraScreen(
                                            analyzerType = AnalyzerType.BARCODE,
                                            onQrScanned = { scanned ->
                                                if (!scanLocked) {
                                                    processQrString(scanned)
                                                }
                                            },
                                            isFlashOn = false,
                                            onCameraReady = { _: CameraControl -> },
                                        )
                                    }
                                    SpacerHeight(4.sdp)
                                    LabelSmallText(
                                        text = "Point camera at QR code",
                                        overrideColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }

                    parseResult?.let { result ->
                        SpacerHeight(8.sdp)
                        HorizontalLineDivider(horizontalPadding = 0.dp)
                        SpacerHeight(8.sdp)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.sdp)
                                .verticalScroll(resultsScrollState),
                        ) {
                            val initiationLabel = when (result.initiationMethod) {
                                "11" -> "Static"
                                "12" -> "Dynamic"
                                else -> result.initiationMethod ?: "Unknown"
                            }
                            LabelMediumText(
                                text = "Type: ${result.qrType} | Initiation: $initiationLabel | CRC: ${if (crcValid) "Valid" else "Invalid"}",
                                overrideColor = MaterialTheme.colorScheme.onPrimary,
                            )
                            SpacerHeight(6.sdp)

                            QrTlvResultsTable(
                                rows = flattenEntriesToTableRows(result.entries),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            SpacerHeight(8.sdp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.sdp),
                            ) {
                                BaseButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    primary = true,
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    onClick = {
                                        val text = buildString {
                                            appendLine("Raw: ${result.rawString}")
                                            append(formatParseResultSummary(result, crcValid))
                                        }
                                        clipboardManager.setText(AnnotatedString(text))
                                        context.showDebugToast("Copied to clipboard")
                                    },
                                ) {
                                    LabelMediumText(
                                        text = "Copy All",
                                        fontSize = 12.textSdp,
                                        overrideColor = MaterialTheme.colorScheme.background,
                                    )
                                }
                                BaseButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    primary = false,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    onClick = {
                                        manualInput = ""
                                        parseResult = null
                                        scanLocked = false
                                    },
                                ) {
                                    LabelMediumText(
                                        text = "Clear",
                                        fontSize = 12.textSdp,
                                        overrideColor = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun QrPasteInputWithCopyAction(
    value: String,
    onValueChange: (String) -> Unit,
    onCopyClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        TextInputFieldApp(
            value = value,
            onValueChange = onValueChange,
            fieldLabel = "Paste QR string",
            placeholder = "Paste raw QR payload here",
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            heightField = 90.sdp,
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 42.sdp, end = 8.sdp)
                .size(24.sdp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .safeClickable { onCopyClick() },
            contentAlignment = Alignment.Center,
        ) {
            ResourceImage(
                image = R.drawable.ic_copy,
                modifier = Modifier.size(14.sdp),
            )
        }
    }
}

@Composable
private fun QrInputModeSelector(
    selectedMode: QrInputMode,
    onModeSelected: (QrInputMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.sdp),
    ) {
        QrInputMode.entries.forEach { mode ->
            val isSelected = selectedMode == mode
            BaseButton(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                primary = isSelected,
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                onClick = { onModeSelected(mode) },
            ) {
                LabelMediumText(
                    text = when (mode) {
                        QrInputMode.Paste -> "Paste String"
                        QrInputMode.Scan -> "Scan Camera"
                    },
                    fontSize = 12.textSdp,
                    overrideColor = if (isSelected) {
                        MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                )
            }
        }
    }
}

@Composable
private fun QrTlvResultsTable(
    rows: List<QRTlvTableRow>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.sdp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.sdp),
        ) {
            QrTlvTableHeaderRow()
            HorizontalLineDivider(horizontalPadding = 0.dp)
            rows.forEachIndexed { index, row ->
                QrTlvTableDataRow(row = row)
                if (index != rows.lastIndex) {
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                }
            }
        }
    }
}

@Composable
private fun QrTlvTableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.sdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QrTlvTableCellFixed(text = "Parent", width = QrTlvTableLayout.parentWidth, isHeader = true)
        QrTlvTableCellFixed(text = "Tag", width = QrTlvTableLayout.tagWidth, isHeader = true)
        QrTlvTableCellFixed(text = "Len", width = QrTlvTableLayout.lengthWidth, isHeader = true)
        QrTlvTableCellFlexible(text = "Value", weight = 0.5f, isHeader = true)
        QrTlvTableCellFlexible(text = "Description", weight = 0.5f, isHeader = true)
    }
}

@Composable
private fun QrTlvTableDataRow(row: QRTlvTableRow) {
    val rowBackground = if (row.isSubTag) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(vertical = 7.sdp),
        verticalAlignment = Alignment.Top,
    ) {
        QrTlvTableCellFixed(
            text = row.parentTag.ifEmpty { "—" },
            width = QrTlvTableLayout.parentWidth,
            isSubTag = row.isSubTag,
        )
        QrTlvTableCellFixed(
            text = row.tag,
            width = QrTlvTableLayout.tagWidth,
            isSubTag = row.isSubTag,
            fontWeight = if (row.isSubTag) FontWeight.Medium else FontWeight.SemiBold,
        )
        QrTlvTableCellFixed(
            text = row.length,
            width = QrTlvTableLayout.lengthWidth,
            isSubTag = row.isSubTag,
        )
        QrTlvTableCellFlexible(text = row.value, weight = 0.5f, isSubTag = row.isSubTag)
        QrTlvTableCellFlexible(text = row.description, weight = 0.5f, isSubTag = row.isSubTag)
    }
}

@Composable
private fun QrTlvTableCellFixed(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isSubTag: Boolean = false,
    fontWeight: FontWeight? = null,
) {
    val styleWeight = fontWeight ?: if (isHeader) FontWeight.Bold else FontWeight.Normal
    val fontSize = if (isHeader) 11.textSdp else 11.textSdp
    val color = when {
        isHeader -> MaterialTheme.colorScheme.onPrimary
        isSubTag -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.sdp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (isHeader) {
            LabelSmallText(
                text = text,
                fontSize = fontSize,
                fontWeight = styleWeight,
                overrideColor = color,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        } else {
            BodySmallText(
                text = text,
                fontSize = fontSize,
                fontWeight = styleWeight,
                overrideColor = color,
                textAlign = TextAlign.Start,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RowScope.QrTlvTableCellFlexible(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    isSubTag: Boolean = false,
    fontWeight: FontWeight? = null,
) {
    val styleWeight = fontWeight ?: if (isHeader) FontWeight.Bold else FontWeight.Normal
    val fontSize = 11.textSdp
    val color = when {
        isHeader -> MaterialTheme.colorScheme.onPrimary
        isSubTag -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onPrimary
    }

    if (isHeader) {
        LabelSmallText(
            text = text,
            fontSize = fontSize,
            fontWeight = styleWeight,
            overrideColor = color,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(weight)
                .padding(horizontal = 4.sdp),
        )
    } else {
        BodySmallText(
            text = text,
            fontSize = fontSize,
            fontWeight = styleWeight,
            overrideColor = color,
            textAlign = TextAlign.Start,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(weight)
                .padding(horizontal = 4.sdp),
        )
    }
}
