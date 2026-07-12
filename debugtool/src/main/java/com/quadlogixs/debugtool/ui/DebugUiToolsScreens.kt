package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quadlogixs.debugtool.R as CommonR
import com.quadlogixs.debugtool.ui.components.AppSwitch
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.LabelLargeText
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.TitleLargeText
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.core.DebugUiToolsStore
import com.quadlogixs.debugtool.core.DynamicTypeLevel
import com.quadlogixs.debugtool.R
import kotlin.math.roundToInt

internal val DebugMenuBackground = Color(0xFFF2F2F7)
internal val DebugSectionCardColor = Color.White
internal val DebugSectionHeaderColor = Color(0xFF8E8E93)
internal val DebugTitleColor = Color(0xFF1C1C1E)
internal val DebugSubtitleColor = Color(0xFF8E8E93)
internal val DebugChevronColor = Color(0xFFC7C7CC)

internal data class DebugMenuItemModel(
    val title: String,
    val subtitle: String = "",
    val icon: Int,
    val iconBackgroundColor: Color,
    val action: DebuggerActions = DebuggerActions.NONE,
    val isToggle: Boolean = false,
)

@Composable
private fun DebugSectionHeaderText(
    text: String,
    overrideColor: Color = DebugSectionHeaderColor,
    modifier: Modifier = Modifier,
) {
    LabelSmallText(
        text = text,
        overrideColor = overrideColor,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(start = 16.dp, bottom = 8.dp),
    )
}

@Composable
internal fun DebugSubMenuHeader(
    title: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E5EA))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            ResourceImage(
                image = R.drawable.ic_back,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(DebugSubtitleColor),
            )
        }

        TitleLargeText(
            text = title,
            overrideColor = DebugTitleColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
        )
    }
}

@Composable
internal fun DebugMenuItemList(
    sectionTitle: String,
    items: List<DebugMenuItemModel>,
    isEncEnable: Boolean = false,
    gridOverlayEnabled: Boolean = false,
    mockLocationEnabled: Boolean = false,
    onItemClick: (DebuggerActions) -> Unit,
    onToggleEnc: (Boolean) -> Unit = {},
    onToggleGrid: (Boolean) -> Unit = {},
    onToggleMockLocation: (Boolean) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
    ) {
        DebugSectionHeaderText(text = sectionTitle, overrideColor = DebugSectionHeaderColor)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DebugSectionCardColor),
        ) {
            items.forEachIndexed { index, item ->
                when {
                    item.isToggle && item.action == DebuggerActions.EncToggle -> {
                        DebugMenuToggleItem(
                            item = item,
                            isChecked = isEncEnable,
                            onCheckedChange = onToggleEnc,
                        )
                    }

                    item.isToggle && item.action == DebuggerActions.LayoutGridOverlay -> {
                        DebugMenuToggleItem(
                            item = item,
                            isChecked = gridOverlayEnabled,
                            onCheckedChange = onToggleGrid,
                        )
                    }

                    item.isToggle && item.action == DebuggerActions.LocationSpoofer -> {
                        DebugMenuToggleItem(
                            item = item,
                            isChecked = mockLocationEnabled,
                            onCheckedChange = onToggleMockLocation,
                        )
                    }

                    else -> {
                        DebugMenuListItem(
                            item = item,
                            onClick = { onItemClick(item.action) },
                        )
                    }
                }

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        thickness = 0.5.dp,
                        color = Color(0xFFE5E5EA),
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugMenuListItem(
    item: DebugMenuItemModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DebugMenuIconTile(icon = item.icon, backgroundColor = item.iconBackgroundColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            LabelLargeText(
                text = item.title,
                overrideColor = DebugTitleColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            if (item.subtitle.isNotBlank()) {
                BodySmallText(
                    text = item.subtitle,
                    overrideColor = DebugSubtitleColor,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        ResourceImage(
            image = R.drawable.ic_arrow_right,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(DebugChevronColor),
        )
    }
}

@Composable
private fun DebugMenuToggleItem(
    item: DebugMenuItemModel,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DebugMenuIconTile(icon = item.icon, backgroundColor = item.iconBackgroundColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            LabelLargeText(
                text = item.title,
                overrideColor = DebugTitleColor,
                fontWeight = FontWeight.Medium,
            )
            BodySmallText(
                text = item.subtitle,
                overrideColor = DebugSubtitleColor,
            )
        }
        AppSwitch(isChecked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun DebugMenuIconTile(
    icon: Int,
    backgroundColor: Color,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        ResourceImage(
            image = icon,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(Color.White),
        )
    }
}

@Composable
fun DebugUiToolsSubMenu(
    onBack: () -> Unit,
    onAction: (DebuggerActions) -> Unit,
) {
    val dynamicType by DebugUiToolsStore.dynamicTypeLevel.collectAsState()
    val animationSpeed by DebugUiToolsStore.animationSpeedScale.collectAsState()
    val gridEnabled by DebugUiToolsStore.gridOverlayEnabled.collectAsState()
    val screenPreset by DebugUiToolsStore.screenSizePreset.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
    ) {
        DebugSubMenuHeader(title = "UI Tools", onBack = onBack)
        DebugMenuItemList(
            sectionTitle = "Tools",
            items = listOf(
                DebugMenuItemModel(
                    title = "Dynamic Type",
                    subtitle = "Current: ${dynamicType.label}",
                    icon = R.drawable.ic_edit_profile,
                    iconBackgroundColor = Color(0xFFAF52DE),
                    action = DebuggerActions.DynamicType,
                ),
                DebugMenuItemModel(
                    title = "Animation Speed",
                    subtitle = "Current: ${formatAnimationSpeed(animationSpeed)}",
                    icon = R.drawable.ic_timer,
                    iconBackgroundColor = Color(0xFFFFCC00),
                    action = DebuggerActions.AnimationSpeed,
                ),
                DebugMenuItemModel(
                    title = "Layout Grid Overlay",
                    subtitle = if (gridEnabled) "8pt grid visible" else "8pt grid drawn over all UI",
                    icon = R.drawable.ic_debug_grid,
                    iconBackgroundColor = Color(0xFFFF2D55),
                    action = DebuggerActions.LayoutGridOverlay,
                    isToggle = true,
                ),
                DebugMenuItemModel(
                    title = "Screen Size Simulator",
                    subtitle = screenPreset?.displayName ?: "Preview layout on different screen sizes",
                    icon = R.drawable.ic_debug_phone,
                    iconBackgroundColor = Color(0xFF007AFF),
                    action = DebuggerActions.ScreenSizeSimulator,
                ),
            ),
            gridOverlayEnabled = gridEnabled,
            onItemClick = onAction,
            onToggleGrid = { DebugUiToolsStore.setGridOverlayEnabled(it) },
        )
    }
}

@Composable
fun DebugDeviceSimulationSubMenu(
    onBack: () -> Unit,
    onAction: (DebuggerActions) -> Unit,
) {
    val mockEnabled by DebugUiToolsStore.mockLocationEnabled.collectAsState()
    val mockLocation by DebugUiToolsStore.mockLocation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
    ) {
        DebugSubMenuHeader(title = "Device Simulation", onBack = onBack)
        DebugMenuItemList(
            sectionTitle = "Simulation",
            items = listOf(
                DebugMenuItemModel(
                    title = "Location Spoofer",
                    subtitle = if (mockEnabled) {
                        "Active: ${mockLocation.label}"
                    } else {
                        "Override device GPS location"
                    },
                    icon = R.drawable.ic_debug_location,
                    iconBackgroundColor = Color(0xFF007AFF),
                    action = DebuggerActions.LocationSpoofer,
                ),
            ),
            mockLocationEnabled = mockEnabled,
            onItemClick = onAction,
            onToggleMockLocation = { DebugUiToolsStore.setMockLocationEnabled(it) },
        )
    }
}

@Composable
fun DynamicTypePickerDialog(
    onDismiss: () -> Unit,
) {
    val current by DebugUiToolsStore.dynamicTypeLevel.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMediumText(text = "Dynamic Type",overrideColor = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                DynamicTypeLevel.entries.forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                DebugUiToolsStore.setDynamicTypeLevel(level)
                                onDismiss()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = current == level,
                            onClick = {
                                DebugUiToolsStore.setDynamicTypeLevel(level)
                                onDismiss()
                            },
                        )
                        LabelMediumText(
                            text = level.label,
                            overrideColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                LabelMediumText(text = "Done",overrideColor = MaterialTheme.colorScheme.onSurface,)
            }
        },
    )
}

@Composable
fun AnimationSpeedPickerDialog(
    onDismiss: () -> Unit,
) {
    val current by DebugUiToolsStore.animationSpeedScale.collectAsState()
    var sliderValue by remember(current) { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMediumText(text = "Animation Speed",overrideColor = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                BodySmallText(
                    text = "Set animation speed (${formatAnimationSpeed(sliderValue)})",
                    overrideColor = DebugSubtitleColor,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        DebugUiToolsStore.setAnimationSpeedScale(it)
                    },
                    valueRange = 0.1f..2f,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LabelSmallText(text = "0.1x", overrideColor = DebugSubtitleColor)
                    LabelSmallText(text = "1x", overrideColor = DebugSubtitleColor)
                    LabelSmallText(text = "2x", overrideColor = DebugSubtitleColor)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                LabelMediumText(text = "Done",overrideColor = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

@Composable
fun ScreenSizeSimulatorDialog(
    onDismiss: () -> Unit,
) {
    val current by DebugUiToolsStore.screenSizePreset.collectAsState()
    var customWidth by remember(current) {
        mutableStateOf(current?.widthDp?.toString().orEmpty().ifBlank { "412" })
    }
    var customHeight by remember(current) {
        mutableStateOf(current?.heightDp?.toString().orEmpty().ifBlank { "915" })
    }
    var customLabel by remember(current) {
        mutableStateOf(
            if (DebugUiToolsStore.isCustomScreenPreset(current)) {
                current?.displayName?.substringBefore(" (").orEmpty()
            } else {
                "Custom"
            },
        )
    }
    var customError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                TitleMediumText(
                    text = "Screen Size Simulator",
                    fontWeight = FontWeight.SemiBold,
                    overrideColor = MaterialTheme.colorScheme.onSurface,
                )
                BodySmallText(
                    text = "Wraps your app in a container sized to the selected device so layout reflows accurately",
                    overrideColor = DebugSubtitleColor,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DebugSelectionChip(
                    label = "Full Screen (Reset)",
                    selected = current == null,
                    onClick = {
                        DebugUiToolsStore.setScreenSizePreset(null)
                        onDismiss()
                    },
                )

                DebugSectionHeaderText(
                    text = "CUSTOM",
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, bottom = 0.dp),
                )

                OutlinedTextField(
                    value = customLabel,
                    onValueChange = { customLabel = it },
                    shape = MaterialTheme.shapes.medium,
                    label = { LabelSmallText(text = "Label (optional)",overrideColor = MaterialTheme.colorScheme.onSurface,) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = customWidth,
                        shape = MaterialTheme.shapes.medium,
                        onValueChange = {
                            customWidth = it.filter { ch -> ch.isDigit() }
                            customError = null
                        },
                        label = { LabelSmallText(text = "Width (dp)",overrideColor = MaterialTheme.colorScheme.onSurface) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    OutlinedTextField(
                        value = customHeight,
                        shape = MaterialTheme.shapes.medium,
                        onValueChange = {
                            customHeight = it.filter { ch -> ch.isDigit() }
                            customError = null
                        },
                        label = { LabelSmallText(text = "Height (dp)",overrideColor = MaterialTheme.colorScheme.onSurface) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }

                LabelSmallText(
                    text = "Range: ${DebugUiToolsStore.MIN_SCREEN_WIDTH_DP}–${DebugUiToolsStore.MAX_SCREEN_WIDTH_DP} w × ${DebugUiToolsStore.MIN_SCREEN_HEIGHT_DP}–${DebugUiToolsStore.MAX_SCREEN_HEIGHT_DP} h dp",
                    overrideColor = DebugSubtitleColor,
                    modifier = Modifier.padding(start = 4.dp)
                )

                if (customError != null) {
                    BodySmallText(
                        text = customError.orEmpty(),
                        overrideColor = Color(0xFFFF3B30),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                DebugSelectionChip(
                    label = "Apply Custom Size",
                    selected = DebugUiToolsStore.isCustomScreenPreset(current),
                    onClick = {
                        val width = customWidth.toIntOrNull()
                        val height = customHeight.toIntOrNull()
                        when {
                            width == null || height == null -> {
                                customError = "Enter valid width and height in dp"
                            }
                            width !in DebugUiToolsStore.MIN_SCREEN_WIDTH_DP..DebugUiToolsStore.MAX_SCREEN_WIDTH_DP ||
                                height !in DebugUiToolsStore.MIN_SCREEN_HEIGHT_DP..DebugUiToolsStore.MAX_SCREEN_HEIGHT_DP -> {
                                customError = "Size is outside the allowed range"
                            }
                            else -> {
                                DebugUiToolsStore.setCustomScreenSize(
                                    widthDp = width,
                                    heightDp = height,
                                    label = customLabel.ifBlank { "Custom" },
                                )
                                onDismiss()
                            }
                        }
                    },
                )

                val presetsByManufacturer = DebugUiToolsStore.screenSizePresets.groupBy { it.manufacturer }
                DebugUiToolsStore.screenSizeManufacturerOrder.forEach { manufacturer ->
                    val presets = presetsByManufacturer[manufacturer].orEmpty()
                    if (presets.isEmpty()) return@forEach

                    DebugSectionHeaderText(
                        text = manufacturer.uppercase(),
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp, bottom = 0.dp),
                    )
                    presets.forEach { preset ->
                        DebugSelectionChip(
                            label = "${preset.displayName}\n${preset.sizeLabel}",
                            selected = current?.id == preset.id,
                            onClick = {
                                DebugUiToolsStore.setScreenSizePreset(preset)
                                onDismiss()
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                LabelMediumText(text = "Cancel",overrideColor = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

@Composable
fun LocationSpooferDialog(
    onDismiss: () -> Unit,
) {
    val current by DebugUiToolsStore.mockLocation.collectAsState()
    val enabled by DebugUiToolsStore.mockLocationEnabled.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleMediumText(text = "Location Spoofer") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BodySmallText(
                    text = if (enabled) "Spoofing active" else "Select a location to spoof",
                    overrideColor = if (enabled) Color(0xFF34C759) else DebugSubtitleColor,
                )
                DebugUiToolsStore.locationPresets.forEach { preset ->
                    DebugSelectionChip(
                        label = "${preset.label}\n${preset.latitude}, ${preset.longitude}",
                        selected = current.id == preset.id,
                        onClick = {
                            DebugUiToolsStore.setMockLocation(preset)
                            DebugUiToolsStore.setMockLocationEnabled(true)
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                LabelMediumText(text = "Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    DebugUiToolsStore.setMockLocationEnabled(false)
                    onDismiss()
                },
            ) {
                LabelMediumText(text = "Disable")
            }
        },
    )
}

@Composable
private fun DebugSelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFE8F0FE) else Color(0xFFF2F2F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LabelMediumText(
            text = label,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            overrideColor = if (selected) Color(0xFF007AFF) else DebugTitleColor,
        )
    }
}

private fun formatAnimationSpeed(scale: Float): String {
    val rounded = (scale * 10f).roundToInt() / 10f
    return "${rounded}x"
}
