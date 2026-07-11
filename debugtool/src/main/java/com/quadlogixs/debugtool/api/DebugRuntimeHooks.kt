package com.quadlogixs.debugtool.api

import kotlinx.coroutines.flow.StateFlow

/**
 * Runtime hooks exposed by :debugTool after [com.quadlogixs.debugtool.DebugTool.install].
 * The host app reads [DebugRuntimeRegistry] and forwards into its own production bridge.
 */
interface DebugRuntimeHooks {
    val typographyScale: StateFlow<Float>
    fun navAnimationDurationMillis(baseMs: Int): Int
    fun mockLocation(): Pair<Double, Double>?
    fun isApiHalted(): Boolean
    fun isLoaderSuppressed(): Boolean
    fun useDebugNavAnimations(): Boolean
}
