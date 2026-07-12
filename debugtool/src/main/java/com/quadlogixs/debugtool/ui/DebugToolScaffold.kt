package com.quadlogixs.debugtool.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.core.DebugApiHalterChecker
import com.quadlogixs.debugtool.core.getCrashCount
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.safeClickable
import kotlin.math.roundToInt
import kotlinx.coroutines.runBlocking

/**
 * How the debug FAB is revealed.
 *
 * [AlwaysVisibleFab] — default; draggable bug icon is always on screen.
 * [ShakeToReveal] — reserved for a future shake-to-show FAB mode (currently same as always-visible).
 */
enum class DebugToolRevealMode {
    AlwaysVisibleFab,
    ShakeToReveal,
}

/**
 * Host-app entry for the full debug UI: screen-size simulator, layout grid,
 * always-visible draggable FAB, menu, and feature dialogs.
 *
 * Must be composed from a Hilt [ComponentActivity] (`@AndroidEntryPoint`) so
 * dialog ViewModels resolve correctly.
 */
@Composable
fun DebugToolScaffold(
    modifier: Modifier = Modifier,
    revealMode: DebugToolRevealMode = DebugToolRevealMode.AlwaysVisibleFab,
    routeTrail: String = "debug",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var offset by remember { mutableStateOf(Offset(48f, 160f)) }
    var menuVisible by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }
    var showCrash by remember { mutableStateOf(false) }
    var showBug by remember { mutableStateOf(false) }
    var showMemory by remember { mutableStateOf(false) }
    var showRecomposition by remember { mutableStateOf(false) }
    var showJank by remember { mutableStateOf(false) }
    var showLogcat by remember { mutableStateOf(false) }
    var showApiPerf by remember { mutableStateOf(false) }
    var showEnv by remember { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    var showMocks by remember { mutableStateOf(false) }

    val fabVisible = when (revealMode) {
        DebugToolRevealMode.AlwaysVisibleFab -> true
        // Future: hide until shake; keep always-visible for this iteration.
        DebugToolRevealMode.ShakeToReveal -> true
    }

    val crashCount = remember(context) { getCrashCount(context) }
    val metadata = remember(crashCount) {
        val host = if (DebugToolRegistry.isInstalled()) DebugToolRegistry.host else null
        val config = if (DebugToolRegistry.isInstalled()) DebugToolRegistry.config else null
        DebugMenuMetadata(
            appVersion = host?.appVersionName().orEmpty(),
            environmentName = host?.environments()?.firstOrNull()?.title
                ?: host?.flavorName().orEmpty(),
            azureLabel = config?.azureLabel.orEmpty(),
            crashCount = crashCount,
        )
    }
    val isEncEnabled = remember(menuVisible) {
        if (DebugToolRegistry.isInstalled()) {
            DebugToolRegistry.host.isEncryptionEnabled(context)
        } else {
            false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        DebugScreenSizeSimulatorFrame(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
                DebugLayoutGridOverlay()
            }
        }

        if (fabVisible) {
            ResourceImage(
                image = if (isActive) R.drawable.ic_bug_active else R.drawable.ic_bug,
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .size(48.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            offset += dragAmount
                            change.consume()
                        }
                    }
                    .safeClickable { menuVisible = !menuVisible },
            )
        }

        HaltApiDialog()

        if (menuVisible) {
            ShowDebugToolMenuDialog(
                haltAllEnabled = DebugApiHalterChecker.haltAllRequestResponseEnabled,
                isEncEnable = isEncEnabled,
                metadata = metadata,
                onDismissRequest = { menuVisible = false },
                onToggleEnc = { enabled ->
                    if (DebugToolRegistry.isInstalled()) {
                        runBlocking {
                            DebugToolRegistry.host.setEncryptionEnabled(context, enabled)
                        }
                        context.showDebugToast("Encryption: $enabled (restart recommended)")
                    }
                },
                onAction = { action ->
                    when (action) {
                        DebuggerActions.HaltRequestResponse -> {
                            DebugApiHalterChecker.haltRequestEnabled = true
                            DebugApiHalterChecker.haltResponseEnabled = true
                            isActive = true
                        }
                        DebuggerActions.HaltAllRequestResponse -> {
                            DebugApiHalterChecker.haltAllRequestResponseEnabled =
                                !DebugApiHalterChecker.haltAllRequestResponseEnabled
                            isActive = DebugApiHalterChecker.haltAllRequestResponseEnabled
                        }
                        DebuggerActions.ReportBug -> showBug = true
                        DebuggerActions.CrashReport -> showCrash = true
                        DebuggerActions.MemoryUsageStats -> showMemory = true
                        DebuggerActions.RecompositionStats -> showRecomposition = true
                        DebuggerActions.JunkStats -> showJank = true
                        DebuggerActions.LocalLogs -> showLogcat = true
                        DebuggerActions.ApiPerformance -> showApiPerf = true
                        DebuggerActions.Environments -> showEnv = true
                        DebuggerActions.ScanQRCode -> showQr = true
                        DebuggerActions.MockResponses -> showMocks = true
                        else -> context.showDebugToast("Action: $action")
                    }
                },
            )
        }

        if (showCrash) CrashLogViewer(onDismissRequest = { showCrash = false })
        if (showMemory) MemoryUsageViewer(onDismissRequest = { showMemory = false })
        if (showRecomposition) RecompositionViewer(onDismissRequest = { showRecomposition = false })
        if (showJank) JunkViewerDialog(onDismissRequest = { showJank = false })
        if (showLogcat && activity != null) {
            LogcatDialog(onDismiss = { showLogcat = false }, viewModel = hiltViewModel())
        }
        if (showApiPerf) ApiPerformanceTestDialog(onDismiss = { showApiPerf = false })
        if (showBug) {
            DebugIssueDialog(
                routeTrail = routeTrail,
                onClickCallBack = { showBug = false },
            )
        }
        if (showEnv) {
            EnvironmentSwitcherDialog(
                onDismiss = { showEnv = false },
                restartApp = {
                    showEnv = false
                    context.showDebugToast("Environment updated")
                },
            )
        }
        if (showQr) QrDebugScanDialog(onDismiss = { showQr = false })
        if (showMocks) MockApiResponsesDialog(onDismiss = { showMocks = false })
    }
}
