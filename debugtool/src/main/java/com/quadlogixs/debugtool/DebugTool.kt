@file:Suppress("DEPRECATION")

package com.quadlogixs.debugtool

import android.app.Activity
import android.app.Application
import android.content.Context
import com.quadlogixs.debugtool.api.DebugNetworkRegistry
import com.quadlogixs.debugtool.api.DebugRuntimeHooks
import com.quadlogixs.debugtool.api.DebugRuntimeRegistry
import com.quadlogixs.debugtool.api.DebugToolConfig
import com.quadlogixs.debugtool.core.DebugToolCore
import com.quadlogixs.debugtool.core.network.ShakeDetector
import com.quadlogixs.debugtool.core.runtime.AppDebugRuntime
import com.quadlogixs.debugtool.hooks.DebugToolHooks
import com.quadlogixs.debugtool.hooks.DebugToolNetwork
import com.quadlogixs.debugtool.ui.ShakeHandle
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object DebugTool {

    private var runtimeHooks: AppDebugRuntime? = null

    fun install(application: Application, config: DebugToolConfig) {
        DebugToolCore.init(application, config)
        val hooks = AppDebugRuntime().also { runtimeHooks = it }
        // Keep deprecated registries wired for v1.0.x hosts.
        DebugRuntimeRegistry.install(hooks)
        DebugToolHooks.installRuntime(hooks)
        DebugNetworkRegistry.install { interceptors, context ->
            DebugToolCore.contributeNetworkInterceptors(interceptors, context)
        }
        DebugToolHooks.installNetworkContributor { interceptors, ctx ->
            DebugToolCore.contributeNetworkInterceptors(interceptors, ctx)
        }
    }

    fun createRuntimeHooks(): DebugRuntimeHooks =
        runtimeHooks ?: AppDebugRuntime().also { runtimeHooks = it }

    fun contributeNetworkInterceptors(
        interceptors: MutableList<Interceptor>,
        context: Context,
    ) {
        DebugToolCore.contributeNetworkInterceptors(interceptors, context)
    }

    fun networkInterceptor(context: Context): Interceptor =
        DebugToolNetwork.interceptor(context)

    fun OkHttpClient.Builder.addDebugToolInterceptors(context: Context): OkHttpClient.Builder =
        with(DebugToolNetwork) { addDebugToolInterceptors(context) }

    /**
     * Optional shake detector wrapping the library [ShakeDetector].
     *
     * Prefer [com.quadlogixs.debugtool.ui.DebugToolScaffold], which auto-starts shake to
     * toggle FAB visibility when [com.quadlogixs.debugtool.ui.DebugToolRevealMode.ShakeToReveal]
     * is set. This helper is for host-owned custom shake handling (Chucker launch is off by default).
     */
    fun createShakeDetector(
        activity: Activity,
        onShake: () -> Unit,
        launchChuckerOnShake: Boolean = false,
    ): ShakeHandle {
        val detector = ShakeDetector(
            context = activity,
            launchChuckerOnShake = launchChuckerOnShake,
            onShakeDetect = onShake,
        )
        return object : ShakeHandle {
            override fun start() = detector.start()
            override fun stop() = detector.stop()
            override fun onShakeDetected() = detector.onShakeDetected()
        }
    }
}
