package com.quadlogixs.debugtool.api

data class DebugToolConfig(
    val host: DebugToolHost,
    val features: DebugFeatureFlags = DebugFeatureFlags(),
    val azure: AzureDevOpsConfig,
    val firestore: FirestoreGateConfig = FirestoreGateConfig(),
    val assignees: List<AssignedTo> = emptyList(),
    val azureLabel: String = "",
)
