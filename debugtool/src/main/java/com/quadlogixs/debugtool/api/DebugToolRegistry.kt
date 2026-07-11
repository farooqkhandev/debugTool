package com.quadlogixs.debugtool.api

object DebugToolRegistry {
    @Volatile
    lateinit var config: DebugToolConfig
        private set

    val host: DebugToolHost
        get() = config.host

    fun install(config: DebugToolConfig) {
        this.config = config
    }

    fun isInstalled(): Boolean = ::config.isInitialized
}
