package com.quadlogixs.debugtool.api

/**
 * Holds the active [DebugRuntimeHooks] after [com.quadlogixs.debugtool.DebugTool.install].
 * The host app reads this and forwards hooks into its own production bridge (e.g. `DebugRuntimeHolder`).
 */
object DebugRuntimeRegistry {

    @Volatile
    private var hooks: DebugRuntimeHooks? = null

    fun install(hooks: DebugRuntimeHooks) {
        this.hooks = hooks
    }

    fun get(): DebugRuntimeHooks? = hooks

    fun clear() {
        hooks = null
    }
}
