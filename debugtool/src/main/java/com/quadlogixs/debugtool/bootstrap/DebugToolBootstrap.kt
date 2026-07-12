package com.quadlogixs.debugtool.bootstrap

import android.app.Application
import com.quadlogixs.debugtool.DebugTool
import com.quadlogixs.debugtool.api.DebugToolConfig

object DebugToolBootstrap {

    /**
     * Initializes the debug tool module and installs runtime / network hooks into
     * [com.quadlogixs.debugtool.hooks.DebugToolHooks] (and deprecated registries).
     *
     * Host apps should wire OkHttp via
     * [com.quadlogixs.debugtool.hooks.DebugToolNetwork.addDebugToolInterceptors]
     * and read runtime values via [com.quadlogixs.debugtool.hooks.DebugToolHooks].
     */
    fun install(application: Application, config: DebugToolConfig) {
        DebugTool.install(application, config)
    }
}
