package com.quadlogixs.debugtool.api

/**
 * Holds the active debug OkHttp contributor after [com.quadlogixs.debugtool.DebugTool.install].
 *
 * @deprecated Prefer [com.quadlogixs.debugtool.hooks.DebugToolNetwork] from `:debugtool-hooks`.
 */
@Deprecated(
    message = "Use DebugToolNetwork / DebugToolHooks from :debugtool-hooks",
    replaceWith = ReplaceWith(
        "DebugToolNetwork",
        "com.quadlogixs.debugtool.hooks.DebugToolNetwork",
    ),
)
object DebugNetworkRegistry {

    @Volatile
    var contributor: DebugNetworkHooks = DebugNetworkHooks { _, _ -> }
        private set

    fun install(contributor: DebugNetworkHooks) {
        this.contributor = contributor
    }

    fun clear() {
        contributor = DebugNetworkHooks { _, _ -> }
        com.quadlogixs.debugtool.hooks.DebugToolHooks.clearNetworkContributor()
    }
}
