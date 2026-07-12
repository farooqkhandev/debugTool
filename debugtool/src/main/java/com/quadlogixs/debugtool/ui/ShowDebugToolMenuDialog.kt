package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.TitleLargeText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.core.ApiSpeedTracker
import com.quadlogixs.debugtool.core.MockApiResponseStore
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme

data class DebugMenuMetadata(
    val appVersion: String = "",
    val environmentName: String = "Default",
    val azureLabel: String = "devops-ais/AikDigital",
    val crashCount: Int = 0,
)

private sealed class DebugMenuDestination {
    data object Main : DebugMenuDestination()
    data object UiTools : DebugMenuDestination()
    data object DeviceSimulation : DebugMenuDestination()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDebugToolMenuDialog(
    haltAllEnabled: Boolean = false,
    isEncEnable: Boolean = false,
    metadata: DebugMenuMetadata = DebugMenuMetadata(),
    onAction: (DebuggerActions) -> Unit,
    onToggleEnc: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    DebugToolTheme {
        ShowDebugToolMenuDialogContent(
            haltAllEnabled = haltAllEnabled,
            isEncEnable = isEncEnable,
            metadata = metadata,
            onAction = onAction,
            onToggleEnc = onToggleEnc,
            onDismissRequest = onDismissRequest,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowDebugToolMenuDialogContent(
    haltAllEnabled: Boolean,
    isEncEnable: Boolean,
    metadata: DebugMenuMetadata,
    onAction: (DebuggerActions) -> Unit,
    onToggleEnc: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentDestination by remember { mutableStateOf<DebugMenuDestination>(DebugMenuDestination.Main) }
    var showDynamicTypeDialog by remember { mutableStateOf(false) }
    var showAnimationSpeedDialog by remember { mutableStateOf(false) }
    var showScreenSizeDialog by remember { mutableStateOf(false) }
    var showLocationSpooferDialog by remember { mutableStateOf(false) }
    var resolvedEnvironmentName by remember { mutableStateOf(metadata.environmentName) }

    LaunchedEffect(metadata.environmentName) {
        resolvedEnvironmentName = resolveSelectedEnvironmentTitle(metadata.environmentName)
    }

    val displayMetadata = remember(metadata, resolvedEnvironmentName) {
        metadata.copy(environmentName = resolvedEnvironmentName)
    }

    val apiRecords by ApiSpeedTracker.records.collectAsState()
    val mockResponses by MockApiResponseStore.mocks.collectAsState()
    val activeMocks = mockResponses.count { it.enabled }
    val sections = remember(haltAllEnabled, isEncEnable, displayMetadata, apiRecords, mockResponses) {
        buildDebugMenuSections(
            haltAllEnabled = haltAllEnabled,
            isEncEnable = isEncEnable,
            metadata = displayMetadata,
            apiRequestCount = apiRecords.size,
            activeMockCount = activeMocks,
        )
    }

    fun handleUiToolAction(action: DebuggerActions) {
        when (action) {
            DebuggerActions.DynamicType -> showDynamicTypeDialog = true
            DebuggerActions.AnimationSpeed -> showAnimationSpeedDialog = true
            DebuggerActions.ScreenSizeSimulator -> showScreenSizeDialog = true
            DebuggerActions.LocationSpoofer -> showLocationSpooferDialog = true
            else -> Unit
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = DebugMenuBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(DebugColors.Border),
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        when (currentDestination) {
            DebugMenuDestination.Main -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp),
                ) {
                    DebugMenuHeader(metadata = displayMetadata, onDismissRequest = onDismissRequest)
                    sections.forEach { section ->
                        DebugMainMenuSection(
                            title = section.title,
                            items = section.items,
                            isEncEnable = isEncEnable,
                            onToggleEnc = {
                                onDismissRequest()
                                onToggleEnc(it)
                            },
                            onItemClick = { action ->
                                when (action) {
                                    DebuggerActions.UiTools -> currentDestination = DebugMenuDestination.UiTools
                                    DebuggerActions.DeviceSimulation -> {
                                        currentDestination = DebugMenuDestination.DeviceSimulation
                                    }
                                    else -> {
                                        onDismissRequest()
                                        onAction(action)
                                    }
                                }
                            },
                        )
                    }
                }
            }

            DebugMenuDestination.UiTools -> {
                DebugUiToolsSubMenu(
                    onBack = { currentDestination = DebugMenuDestination.Main },
                    onAction = ::handleUiToolAction,
                )
            }

            DebugMenuDestination.DeviceSimulation -> {
                DebugDeviceSimulationSubMenu(
                    onBack = { currentDestination = DebugMenuDestination.Main },
                    onAction = ::handleUiToolAction,
                )
            }
        }
    }

    if (showDynamicTypeDialog) {
        DynamicTypePickerDialog(onDismiss = { showDynamicTypeDialog = false })
    }
    if (showAnimationSpeedDialog) {
        AnimationSpeedPickerDialog(onDismiss = { showAnimationSpeedDialog = false })
    }
    if (showScreenSizeDialog) {
        ScreenSizeSimulatorDialog(onDismiss = { showScreenSizeDialog = false })
    }
    if (showLocationSpooferDialog) {
        LocationSpooferDialog(onDismiss = { showLocationSpooferDialog = false })
    }
}

@Composable
private fun DebugMenuHeader(
    metadata: DebugMenuMetadata,
    onDismissRequest: () -> Unit,
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
                .background(DebugCloseButtonBg)
                .clickable(onClick = onDismissRequest),
            contentAlignment = Alignment.Center,
        ) {
            ResourceImage(
                image = R.drawable.ic_close_receipt,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(DebugTitleColor),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TitleLargeText(
                text = "Debug Toolkit",
                overrideColor = DebugTitleColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            LabelSmallText(
                text = "v${metadata.appVersion} · ${metadata.environmentName} Environment",
                overrideColor = DebugSubtitleColor,
                textAlign = TextAlign.Center,
            )
            LabelSmallText(
                text = "Azure: ${metadata.azureLabel}",
                overrideColor = DebugColors.AccentTeal,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DebugMainMenuSection(
    title: String,
    items: List<DebugMenuItemModel>,
    isEncEnable: Boolean,
    onToggleEnc: (Boolean) -> Unit,
    onItemClick: (DebuggerActions) -> Unit,
) {
    DebugMenuItemList(
        sectionTitle = title,
        items = items,
        isEncEnable = isEncEnable,
        onItemClick = onItemClick,
        onToggleEnc = onToggleEnc,
    )
}

private data class DebugMenuSectionModel(
    val title: String,
    val items: List<DebugMenuItemModel>,
)

private fun buildDebugMenuSections(
    haltAllEnabled: Boolean,
    isEncEnable: Boolean,
    metadata: DebugMenuMetadata,
    apiRequestCount: Int,
    activeMockCount: Int,
): List<DebugMenuSectionModel> = listOf(
    DebugMenuSectionModel(
        title = "REPORTING",
        items = listOf(
            DebugMenuItemModel(
                title = "Report Bug (Azure DevOps)",
                subtitle = metadata.azureLabel,
                icon = R.drawable.ic_bug_report,
                iconBackgroundColor = DebugColors.AccentTeal,
                action = DebuggerActions.ReportBug,
            ),
            DebugMenuItemModel(
                title = "Crash Reports",
                subtitle = "${metadata.crashCount} crash(es) saved",
                icon = R.drawable.ic_crash,
                iconBackgroundColor = DebugColors.AccentOrange,
                action = DebuggerActions.CrashReport,
            ),
        ),
    ),
    DebugMenuSectionModel(
        title = "NETWORK",
        items = listOf(
            DebugMenuItemModel(
                title = "Network Trace",
                subtitle = "Open Chucker network inspector",
                icon = R.drawable.ic_network_trace,
                iconBackgroundColor = DebugColors.AccentBlue,
                action = DebuggerActions.NetworkTrace,
            ),
            DebugMenuItemModel(
                title = "Halt & Edit API",
                subtitle = "Intercept and modify requests",
                icon = R.drawable.ic_api_halt,
                iconBackgroundColor = DebugColors.AccentPurple,
                action = DebuggerActions.HaltRequestResponse,
            ),
            DebugMenuItemModel(
                title = "Block All Requests",
                subtitle = if (haltAllEnabled) "Blocking all traffic" else "Passing through",
                icon = R.drawable.ic_api_halt,
                iconBackgroundColor = DebugColors.AccentRed,
                action = DebuggerActions.HaltAllRequestResponse,
            ),
            DebugMenuItemModel(
                title = "API Performance",
                subtitle = if (apiRequestCount > 0) {
                    "$apiRequestCount request(s) analyzed"
                } else {
                    "No requests analyzed yet"
                },
                icon = R.drawable.ic_api_halt,
                iconBackgroundColor = DebugColors.AccentTeal,
                action = DebuggerActions.ApiPerformance,
            ),
            DebugMenuItemModel(
                title = "Response Mocker",
                subtitle = if (activeMockCount > 0) {
                    "$activeMockCount active mock(s)"
                } else {
                    "No active mocks"
                },
                icon = R.drawable.ic_api_halt,
                iconBackgroundColor = DebugColors.AccentGreen,
                action = DebuggerActions.MockResponses,
            ),
            DebugMenuItemModel(
                title = "Encryption",
                subtitle = if (isEncEnable) "Encryption enabled" else "Encryption disabled",
                icon = R.drawable.ic_security,
                iconBackgroundColor = DebugColors.AccentOrange,
                action = DebuggerActions.EncToggle,
                isToggle = true,
            ),
        ),
    ),
    DebugMenuSectionModel(
        title = "UI TOOLS",
        items = listOf(
            DebugMenuItemModel(
                title = "UI Tools",
                subtitle = "Dynamic Type, animations, grid, screen size",
                icon = R.drawable.ic_debug_ui_tools,
                iconBackgroundColor = DebugColors.AccentPurple,
                action = DebuggerActions.UiTools,
            ),
            DebugMenuItemModel(
                title = "Device Simulation",
                subtitle = "Location spoofer",
                icon = R.drawable.ic_debug_location,
                iconBackgroundColor = DebugColors.AccentBlue,
                action = DebuggerActions.DeviceSimulation,
            ),
        ),
    ),
    DebugMenuSectionModel(
        title = "PERFORMANCE",
        items = listOf(
            DebugMenuItemModel(
                title = "Memory Usage",
                subtitle = "Live heap & allocation stats",
                icon = R.drawable.ic_memory_usage,
                iconBackgroundColor = DebugColors.AccentPurple,
                action = DebuggerActions.MemoryUsageStats,
            ),
            DebugMenuItemModel(
                title = "Recomposition Stats",
                subtitle = "Compose recomposition tracker",
                icon = R.drawable.ic_recompose,
                iconBackgroundColor = DebugColors.AccentBlue,
                action = DebuggerActions.RecompositionStats,
            ),
            DebugMenuItemModel(
                title = "Jank Stats",
                subtitle = "Frame timing & jank logs",
                icon = R.drawable.ic_recompose,
                iconBackgroundColor = DebugColors.AccentOrange,
                action = DebuggerActions.JunkStats,
            ),
        ),
    ),
    DebugMenuSectionModel(
        title = "LOGS",
        items = listOf(
            DebugMenuItemModel(
                title = "Local Logs",
                subtitle = "View on-device logcat",
                icon = R.drawable.ic_logs,
                iconBackgroundColor = DebugColors.SurfaceElevated,
                action = DebuggerActions.LocalLogs,
            ),
            DebugMenuItemModel(
                title = "Remote Logs",
                subtitle = "Fetch server-side logs",
                icon = R.drawable.ic_logs,
                iconBackgroundColor = DebugColors.TextSecondary,
                action = DebuggerActions.RemoteLogs,
            ),
        ),
    ),
    DebugMenuSectionModel(
        title = "CONFIGURATION",
        items = listOf(
            DebugMenuItemModel(
                title = "Change Environment",
                subtitle = metadata.environmentName,
                icon = R.drawable.ic_network_trace,
                iconBackgroundColor = DebugColors.AccentTeal,
                action = DebuggerActions.Environments,
            ),
            DebugMenuItemModel(
                title = "Scan QR Code",
                subtitle = "Import debug configuration",
                icon = R.drawable.ic_upload_qr,
                iconBackgroundColor = DebugColors.AccentGreen,
                action = DebuggerActions.ScanQRCode,
            ),
        ),
    ),
)

enum class DebuggerActions {
    ReportBug,
    NetworkTrace,
    CrashReport,
    HaltRequestResponse,
    HaltAllRequestResponse,
    MemoryUsageStats,
    RecompositionStats,
    JunkStats,
    EncToggle,
    LocalLogs,
    RemoteLogs,
    ApiPerformance,
    Environments,
    ScanQRCode,
    MockResponses,
    UiTools,
    DeviceSimulation,
    DynamicType,
    AnimationSpeed,
    LayoutGridOverlay,
    ScreenSizeSimulator,
    LocationSpoofer,
    NONE,
}

/**
 * Prefers the selected runtime environment title from [DebugToolHost], so the menu
 * does not show the build flavor (e.g. DEV) when QA/UAT/etc. is selected.
 */
private suspend fun resolveSelectedEnvironmentTitle(fallback: String): String {
    if (!DebugToolRegistry.isInstalled()) return fallback
    return runCatching {
        val host = DebugToolRegistry.host
        val environments = host.environments()
        val currentUrl = host.currentEnvironment().trimEnd('/')
        if (currentUrl.isBlank() || currentUrl.equals("default", ignoreCase = true)) {
            return@runCatching environments.firstOrNull()?.title ?: fallback
        }
        environments
            .firstOrNull { it.url.trimEnd('/') == currentUrl }
            ?.title
            ?: fallback
    }.getOrDefault(fallback)
}
