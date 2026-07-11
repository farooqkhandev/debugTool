package com.quadlogixs.debugtool.api

interface DebugToolHost {
    fun environments(): List<DebugEnvironment>

    suspend fun currentEnvironment(): String

    suspend fun applyEnvironment(url: String)

    fun appVersionName(): String

    fun flavorName(): String

    fun isEncryptionEnabled(context: android.content.Context): Boolean

    suspend fun setEncryptionEnabled(context: android.content.Context, enabled: Boolean)

    fun deviceId(): String
}
