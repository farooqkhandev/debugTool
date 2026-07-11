package com.quadlogixs.debugtool.bootstrap

import android.app.Application
import com.quadlogixs.debugtool.DebugTool
import com.quadlogixs.debugtool.api.DebugToolConfig
import com.quadlogixs.debugtool.api.DebugRuntimeRegistry
import com.quadlogixs.debugtool.api.DebugNetworkRegistry

object DebugToolBootstrap {

    /**
     * Initializes the debug tool module. Does not wire production bridges —
     * the host app must connect [DebugRuntimeRegistry] and [DebugNetworkRegistry]
     * to its own production bridge types after calling this.
     */
    fun install(application: Application, config: DebugToolConfig) {
        DebugTool.install(application, config)
    }
}
