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
     * FAB reveal is always-on via [com.quadlogixs.debugtool.ui.DebugToolScaffold];
     * shake is typically used to open Chucker / custom callbacks.
     */
    fun createShakeDetector(activity: Activity, onShake: () -> Unit): ShakeHandle {
        val detector = ShakeDetector(activity, onShakeDetect = onShake)
        return object : ShakeHandle {
            override fun start() = detector.start()
            override fun stop() = detector.stop()
            override fun onShakeDetected() = detector.onShakeDetected()
        }
    }
}
