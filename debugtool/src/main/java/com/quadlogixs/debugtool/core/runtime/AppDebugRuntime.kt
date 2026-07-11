package com.quadlogixs.debugtool.core.runtime

import com.quadlogixs.debugtool.api.DebugRuntimeHooks
import com.quadlogixs.debugtool.core.DebugApiHalterChecker
import com.quadlogixs.debugtool.core.DebugUiToolsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AppDebugRuntime : DebugRuntimeHooks {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _typographyScale = MutableStateFlow(1f)

    init {
        DebugUiToolsStore.dynamicTypeLevel
            .onEach { _typographyScale.value = it.scale }
            .launchIn(scope)
    }

    override val typographyScale: StateFlow<Float> = _typographyScale.asStateFlow()

    override fun navAnimationDurationMillis(baseMs: Int): Int =
        DebugUiToolsStore.debugAnimDurationMillis(baseMs)

    override fun mockLocation(): Pair<Double, Double>? {
        if (!DebugUiToolsStore.mockLocationEnabled.value) return null
        val mock = DebugUiToolsStore.mockLocation.value
        return mock.latitude to mock.longitude
    }

    override fun isApiHalted(): Boolean =
        DebugApiHalterChecker.haltRequestEnabled ||
            DebugApiHalterChecker.haltResponseEnabled ||
            DebugApiHalterChecker.haltAllRequestResponseEnabled

    override fun isLoaderSuppressed(): Boolean = isApiHalted()

    override fun useDebugNavAnimations(): Boolean = true
}
