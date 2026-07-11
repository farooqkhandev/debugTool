package com.quadlogixs.debugtool.api

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
    val azureLabel: String = "",
)
