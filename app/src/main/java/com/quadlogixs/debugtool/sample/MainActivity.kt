package com.quadlogixs.debugtool.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quadlogixs.debugtool.core.DebugApiHalterChecker
import com.quadlogixs.debugtool.core.getCrashCount
import com.quadlogixs.debugtool.ui.ApiPerformanceTestDialog
import com.quadlogixs.debugtool.ui.CrashLogViewer
import com.quadlogixs.debugtool.ui.DebugIssueDialog
import com.quadlogixs.debugtool.ui.DebugMenuMetadata
import com.quadlogixs.debugtool.ui.DebuggerActions
import com.quadlogixs.debugtool.ui.EnvironmentSwitcherDialog
import com.quadlogixs.debugtool.ui.HaltApiDialog
import com.quadlogixs.debugtool.ui.JunkViewerDialog
import com.quadlogixs.debugtool.ui.LogcatDialog
import com.quadlogixs.debugtool.ui.MemoryUsageViewer
import com.quadlogixs.debugtool.ui.MockApiResponsesDialog
import com.quadlogixs.debugtool.ui.QrDebugScanDialog
import com.quadlogixs.debugtool.ui.RecompositionViewer
import com.quadlogixs.debugtool.ui.ShowDebugToolMenuDialog
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.showDebugToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sampleHost: SampleDebugToolHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen(host = sampleHost)
                }
            }
        }
    }
}

@Composable
private fun SampleScreen(host: SampleDebugToolHost) {
    val activity = androidx.compose.ui.platform.LocalContext.current as ComponentActivity
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

    val crashCount = remember { getCrashCount(activity) }
    val metadata = remember(crashCount) {
        DebugMenuMetadata(
            appVersion = BuildConfig.VERSION_NAME,
            environmentName = host.flavorName(),
            azureLabel = "sample/debugTool",
            crashCount = crashCount,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "debugTool Sample",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Shake is optional. Tap the floating bug icon to open the debug menu.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        ResourceImage(
            image = if (isActive) {
                com.quadlogixs.debugtool.R.drawable.ic_bug_active
            } else {
                com.quadlogixs.debugtool.R.drawable.ic_bug
            },
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

        HaltApiDialog()

        if (menuVisible) {
            ShowDebugToolMenuDialog(
                haltAllEnabled = DebugApiHalterChecker.haltAllRequestResponseEnabled,
                isEncEnable = host.isEncryptionEnabled(activity),
                metadata = metadata,
                onDismissRequest = { menuVisible = false },
                onToggleEnc = { enabled ->
                    runBlocking { host.setEncryptionEnabled(activity, enabled) }
                    activity.showDebugToast("Encryption: $enabled (restart recommended)")
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
                        else -> activity.showDebugToast("Action: $action")
                    }
                },
            )
        }

        if (showCrash) CrashLogViewer(onDismissRequest = { showCrash = false })
        if (showMemory) MemoryUsageViewer(onDismissRequest = { showMemory = false })
        if (showRecomposition) RecompositionViewer(onDismissRequest = { showRecomposition = false })
        if (showJank) JunkViewerDialog(onDismissRequest = { showJank = false })
        if (showLogcat) LogcatDialog(onDismiss = { showLogcat = false }, viewModel = hiltViewModel())
        if (showApiPerf) ApiPerformanceTestDialog(onDismiss = { showApiPerf = false })
        if (showBug) {
            DebugIssueDialog(
                routeTrail = "sample → MainActivity",
                onClickCallBack = { showBug = false },
            )
        }
        if (showEnv) {
            EnvironmentSwitcherDialog(
                onDismiss = { showEnv = false },
                restartApp = {
                    showEnv = false
                    activity.showDebugToast("Environment updated")
                },
            )
        }
        if (showQr) QrDebugScanDialog(onDismiss = { showQr = false })
        if (showMocks) MockApiResponsesDialog(onDismiss = { showMocks = false })
    }
}
