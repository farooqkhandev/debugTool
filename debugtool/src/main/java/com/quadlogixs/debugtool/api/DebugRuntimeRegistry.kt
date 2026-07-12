package com.quadlogixs.debugtool.api

/**
 * Holds the active [DebugRuntimeHooks] after [com.quadlogixs.debugtool.DebugTool.install].
 *
 * @deprecated Prefer [com.quadlogixs.debugtool.hooks.DebugToolHooks] from `:debugtool-hooks`.
 */
@Deprecated(
    message = "Use DebugToolHooks from :debugtool-hooks",
    replaceWith = ReplaceWith(
        "DebugToolHooks",
        "com.quadlogixs.debugtool.hooks.DebugToolHooks",
    ),
)
object DebugRuntimeRegistry {

    @Volatile
    private var hooks: DebugRuntimeHooks? = null

    fun install(hooks: DebugRuntimeHooks) {
        this.hooks = hooks
    }

    fun get(): DebugRuntimeHooks? = hooks

    fun clear() {
        hooks = null
        com.quadlogixs.debugtool.hooks.DebugToolHooks.clearRuntime()
    }
}
