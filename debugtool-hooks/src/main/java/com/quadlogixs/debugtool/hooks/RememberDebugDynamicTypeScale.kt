package com.quadlogixs.debugtool.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Collects [DebugToolHooks.typographyScale] for Compose (default `1f` when no runtime installed).
 */
@Composable
fun rememberDebugDynamicTypeScale(): Float {
    val scale by DebugToolHooks.typographyScale.collectAsState()
    return scale
}

/**
 * Same as [rememberDebugDynamicTypeScale] but returns a [State] for callers that need it.
 */
@Composable
fun rememberDebugDynamicTypeScaleState(): State<Float> =
    DebugToolHooks.typographyScale.collectAsState()
