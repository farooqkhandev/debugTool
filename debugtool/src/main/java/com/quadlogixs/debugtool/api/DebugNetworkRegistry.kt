package com.quadlogixs.debugtool.api

/**
 * Holds the active debug OkHttp contributor after [com.quadlogixs.debugtool.DebugTool.install].
 * The host app reads this and forwards into its own production bridge (e.g. `DebugNetworkBridge`).
 */
object DebugNetworkRegistry {

    @Volatile
    var contributor: DebugNetworkHooks = DebugNetworkHooks { _, _ -> }
        private set

    fun install(contributor: DebugNetworkHooks) {
        this.contributor = contributor
    }

    fun clear() {
        contributor = DebugNetworkHooks { _, _ -> }
    }
}
