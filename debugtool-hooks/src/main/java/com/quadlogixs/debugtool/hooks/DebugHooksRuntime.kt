package com.quadlogixs.debugtool.hooks

import kotlinx.coroutines.flow.StateFlow

/**
 * Runtime delegate installed by the full `:debugtool` library.
 * Host apps should read values via [DebugToolHooks], not this interface.
 */
interface DebugHooksRuntime {
    val typographyScale: StateFlow<Float>
    fun navAnimationDurationMillis(baseMs: Int): Int
    fun mockLocation(): Pair<Double, Double>?
    fun isApiHalted(): Boolean
    fun isLoaderSuppressed(): Boolean
    fun useDebugNavAnimations(): Boolean
}
