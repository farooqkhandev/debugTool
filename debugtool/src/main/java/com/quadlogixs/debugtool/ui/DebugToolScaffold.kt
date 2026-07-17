package com.quadlogixs.debugtool.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.metrics.performance.JankStats
import com.chuckerteam.chucker.api.Chucker
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.core.DebugApiHalterChecker
import com.quadlogixs.debugtool.core.DebugEnvironmentStore
import com.quadlogixs.debugtool.core.di.DebugJankEntryPoint
import com.quadlogixs.debugtool.core.getCrashCount
import com.quadlogixs.debugtool.core.network.ShakeDetector
import com.quadlogixs.debugtool.hooks.DebugToolHooks
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.theme.DebugTheme
import dagger.hilt.android.EntryPointAccessors
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * How the debug FAB is revealed.
 *
 * [AlwaysVisibleFab] — FAB stays visible (ignores [com.quadlogixs.debugtool.api.DebugToolConfig.showFabOnLaunch]).
 * [ShakeToReveal] — FAB starts from [com.quadlogixs.debugtool.api.DebugToolConfig.showFabOnLaunch]
 * (default hidden); shake toggles visibility when [com.quadlogixs.debugtool.api.DebugFeatureFlags.shakeEnabled].
 */
enum class DebugToolRevealMode {
    AlwaysVisibleFab,
    ShakeToReveal,
}

/**
 * Host-app entry for the full debug UI: optional Dynamic Type density for [content],
 * screen-size simulator, layout grid, shake-to-reveal FAB, menu, and feature dialogs.
 *
 * **Theme isolation:** host [content] is never wrapped in [DebugTheme] / MaterialTheme.
 * Only debug overlays (menu / dialogs) use [DebugTheme], so the host primary color
 * (e.g. yellow Login button) is not replaced by the library palette.
 *
 * Must be composed from a Hilt [ComponentActivity] (`@AndroidEntryPoint`) so
 * dialog ViewModels resolve correctly.
 */
@Composable
fun DebugToolScaffold(
    modifier: Modifier = Modifier,
    revealMode: DebugToolRevealMode = DebugToolRevealMode.ShakeToReveal,
    routeTrail: String = "debug",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()

    val config = if (DebugToolRegistry.isInstalled()) DebugToolRegistry.config else null
    val showFabOnLaunch = config?.showFabOnLaunch == true
    val fabIconRes = config?.fabIconRes ?: R.drawable.ic_bug
    val fabActiveIconRes = config?.fabActiveIconRes ?: R.drawable.ic_bug_active
    val chuckerEnabled = config?.features?.chuckerEnabled == true

    var offset by remember { mutableStateOf(Offset(48f, 160f)) }
    var fabVisible by remember(revealMode, showFabOnLaunch) {
        mutableStateOf(
            when (revealMode) {
                DebugToolRevealMode.AlwaysVisibleFab -> true
                DebugToolRevealMode.ShakeToReveal -> showFabOnLaunch
            },
        )
    }
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

    val typographyScale by DebugToolHooks.typographyScale.collectAsState()
    val density = LocalDensity.current
    val scaledDensity = remember(density, typographyScale) {
        Density(density.density, density.fontScale * typographyScale)
    }

    val crashCount = remember(context) { getCrashCount(context) }
    val metadata = remember(crashCount) {
        val host = if (DebugToolRegistry.isInstalled()) DebugToolRegistry.host else null
        val cfg = if (DebugToolRegistry.isInstalled()) DebugToolRegistry.config else null
        val azure = cfg?.azure
        val label = cfg?.azureLabel?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(azure?.organization, azure?.project)
                .filter { it.isNotBlank() }
                .joinToString("/")
        DebugMenuMetadata(
            appVersion = host?.appVersionName().orEmpty(),
            // Menu LaunchedEffect resolves the real title via DebugEnvironmentStore.
            environmentName = host?.flavorName().orEmpty(),
            azureLabel = label,
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

    // Periodic memory sampling while the scaffold is shown.
    DisposableEffect(routeTrail) {
        val job = scope.launch {
            while (isActive) {
                MemoryTracker.logMemory(routeTrail)
                delay(5_000L)
            }
        }
        onDispose { job.cancel() }
    }

    // JankStats: Activity Hilt module provides it, but nothing injected it — start tracking here.
    DisposableEffect(activity) {
        var jankStats: JankStats? = null
        if (activity != null) {
            runCatching {
                val entryPoint = EntryPointAccessors.fromApplication(
                    activity.applicationContext,
                    DebugJankEntryPoint::class.java,
                )
                jankStats = JankStats.createAndTrack(activity.window, entryPoint.frameDataProvider())
                jankStats?.isTrackingEnabled = true
            }.onFailure {
                Timber.tag("DebugToolScaffold").w(it, "JankStats tracking unavailable")
            }
        }
        onDispose {
            runCatching { jankStats?.isTrackingEnabled = false }
        }
    }

    // Shake toggles FAB visibility (Chucker opens from Network Trace menu).
    DisposableEffect(activity, revealMode, config?.features?.shakeEnabled) {
        val shakeEnabled = config?.features?.shakeEnabled == true
        val shouldListen = shakeEnabled &&
            activity != null &&
            revealMode != DebugToolRevealMode.AlwaysVisibleFab
        if (!shouldListen) {
            return@DisposableEffect onDispose { }
        }
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        val detector = ShakeDetector(
            context = activity!!.applicationContext,
            launchChuckerOnShake = false,
            onShakeDetect = {
                // Sensor callbacks are not always on the main thread.
                mainHandler.post { fabVisible = !fabVisible }
            },
        )
        detector.start()
        onDispose {
            detector.stop()
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    fun openNetworkTrace() {
        if (!chuckerEnabled) {
            context.showDebugToast("Chucker is disabled")
            return
        }
        val launchContext = activity ?: context
        runCatching {
            launchContext.startActivity(
                Chucker.getLaunchIntent(launchContext).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )
        }.onFailure {
            Timber.tag("DebugToolScaffold").e(it, "Failed to launch Chucker")
            context.showDebugToast("Unable to open Network Trace")
        }
    }

    fun handleDebugAction(action: DebuggerActions) {
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
            DebuggerActions.NetworkTrace -> openNetworkTrace()
            DebuggerActions.EncToggle,
            DebuggerActions.UiTools,
            DebuggerActions.DeviceSimulation,
            DebuggerActions.DynamicType,
            DebuggerActions.AnimationSpeed,
            DebuggerActions.LayoutGridOverlay,
            DebuggerActions.ScreenSizeSimulator,
            DebuggerActions.LocationSpoofer,
            DebuggerActions.RemoteLogs,
            DebuggerActions.NONE,
            -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Host content keeps the host MaterialTheme. Density is only overridden when
        // Dynamic Type scale != 1f so default install does not touch host layout metrics.
        val hostContent: @Composable () -> Unit = {
            DebugScreenSizeSimulatorFrame(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                    DebugLayoutGridOverlay()
                }
            }
        }
        if (typographyScale == 1f) {
            hostContent()
        } else {
            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                hostContent()
            }
        }

        // FAB: no MaterialTheme — must not tint host colors.
        RecompositionLogger(fallbackRoute = routeTrail)

        if (fabVisible) {
            ResourceImage(
                image = if (isActive) fabActiveIconRes else fabIconRes,
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

        // DebugTheme ONLY around library overlays — never around [content].
        DebugTheme {
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
                    onAction = { handleDebugAction(it) },
                )
            }

            if (showCrash) CrashLogViewer(onDismissRequest = { showCrash = false })
            if (showMemory) {
                MemoryUsageViewer(
                    onDismissRequest = { showMemory = false },
                    routeTrail = routeTrail,
                )
            }
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
}
