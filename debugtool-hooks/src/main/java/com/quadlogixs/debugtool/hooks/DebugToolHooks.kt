package com.quadlogixs.debugtool.hooks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Always-safe runtime hooks for host apps.
 *
 * - Depend on `:debugtool-hooks` from **all** build types (implementation / releaseImplementation).
 * - Install the full `:debugtool` only via **debugImplementation**.
 * - When full debugtool is absent, every accessor returns a no-op default.
 *
 * Nav animation duration: hosts should call
 * `DebugToolHooks.navAnimationDurationMillis(base)` (duration via hooks only in this iteration).
 */
object DebugToolHooks {

    private val defaultTypographyScale: StateFlow<Float> = MutableStateFlow(1f).asStateFlow()

    @Volatile
    private var runtime: DebugHooksRuntime? = null

    @Volatile
    private var networkContributor: DebugNetworkContributor? = null

    val typographyScale: StateFlow<Float>
        get() = runtime?.typographyScale ?: defaultTypographyScale

    fun navAnimationDurationMillis(baseMs: Int): Int =
        runtime?.navAnimationDurationMillis(baseMs) ?: baseMs

    fun mockLocation(): Pair<Double, Double>? =
        runtime?.mockLocation()

    fun isLoaderSuppressed(): Boolean =
        runtime?.isLoaderSuppressed() ?: false

    fun useDebugNavAnimations(): Boolean =
        runtime?.useDebugNavAnimations() ?: false

    fun isApiHalted(): Boolean =
        runtime?.isApiHalted() ?: false

    /**
     * Installed by `:debugtool` only. Not for host apps.
     */
    @JvmSynthetic
    fun installRuntime(delegate: DebugHooksRuntime) {
        runtime = delegate
    }

    /**
     * Cleared by `:debugtool` only. Not for host apps.
     */
    @JvmSynthetic
    fun clearRuntime() {
        runtime = null
    }

    /**
     * Installed by `:debugtool` only. Not for host apps.
     */
    @JvmSynthetic
    fun installNetworkContributor(contributor: DebugNetworkContributor) {
        networkContributor = contributor
    }

    /**
     * Cleared by `:debugtool` only. Not for host apps.
     */
    @JvmSynthetic
    fun clearNetworkContributor() {
        networkContributor = null
    }

    @JvmSynthetic
    fun networkContributorOrNull(): DebugNetworkContributor? = networkContributor
}
