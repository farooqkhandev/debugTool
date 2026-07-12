package com.quadlogixs.debugtool.api

data class DebugFeatureFlags(
    val shakeEnabled: Boolean = true,
    val chuckerEnabled: Boolean = true,
    val firestoreGateEnabled: Boolean = true,
    val mockApiPersistenceEnabled: Boolean = true,
)
