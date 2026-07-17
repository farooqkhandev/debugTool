package com.quadlogixs.debugtool.api

/**
 * Install-time configuration for the debug tool.
 *
 * FAB visibility: [showFabOnLaunch] defaults to `false` (hidden until shake when using
 * [com.quadlogixs.debugtool.ui.DebugToolRevealMode.ShakeToReveal]). Optionally override FAB
 * drawables via [fabIconRes] / [fabActiveIconRes] (host module `R.drawable` ids).
 */
data class DebugToolConfig(
    val host: DebugToolHost,
    val features: DebugFeatureFlags = DebugFeatureFlags(),
    val azure: AzureDevOpsConfig,
    val firestore: FirestoreGateConfig = FirestoreGateConfig(),
    /**
     * Fallback Assigned To roster when [DebugToolHost.assignees] is empty.
     * Prefer implementing [DebugToolHost.assignees] in the host app (e.g. ZIslamic).
     */
    val assignees: List<AssignedTo> = emptyList(),
    /**
     * Optional menu label. When blank, the scaffold derives `"organization/project"` from [azure].
     */
    val azureLabel: String = "",
    /**
     * When `true`, the debug FAB is shown as soon as [com.quadlogixs.debugtool.ui.DebugToolScaffold]
     * is composed (shake still toggles visibility in [ShakeToReveal] mode).
     * Default `false` — shake to reveal.
     */
    val showFabOnLaunch: Boolean = false,
    /**
     * Optional host drawable for the inactive FAB. `null` uses the library default bug icon.
     */
    val fabIconRes: Int? = null,
    /**
     * Optional host drawable for the active (halt/busy) FAB. `null` uses the library default.
     */
    val fabActiveIconRes: Int? = null,
)
