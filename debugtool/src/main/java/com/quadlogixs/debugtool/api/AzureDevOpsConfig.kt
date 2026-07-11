package com.quadlogixs.debugtool.api

fun interface AzurePatProvider {
    fun getPat(): String
}

data class AzureDevOpsConfig(
    val organization: String,
    val project: String,
    val areaPath: String,
    val patProvider: AzurePatProvider,
)
