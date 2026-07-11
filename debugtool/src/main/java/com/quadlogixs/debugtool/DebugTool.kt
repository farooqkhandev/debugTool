package com.quadlogixs.debugtool

import android.app.Application
import android.content.Context
import com.quadlogixs.debugtool.api.DebugNetworkRegistry
import com.quadlogixs.debugtool.api.DebugRuntimeRegistry
import com.quadlogixs.debugtool.api.DebugToolConfig
import com.quadlogixs.debugtool.api.DebugRuntimeHooks
import com.quadlogixs.debugtool.core.DebugToolCore
import com.quadlogixs.debugtool.core.runtime.AppDebugRuntime
import okhttp3.Interceptor

object DebugTool {

    private var runtimeHooks: AppDebugRuntime? = null

    fun install(application: Application, config: DebugToolConfig) {
        DebugToolCore.init(application, config)
        val hooks = AppDebugRuntime().also { runtimeHooks = it }
        DebugRuntimeRegistry.install(hooks)
        DebugNetworkRegistry.install { interceptors, context ->
            DebugToolCore.contributeNetworkInterceptors(interceptors, context)
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
}
