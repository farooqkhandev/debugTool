package com.quadlogixs.debugtool.core.data.dto

data class AzurePatchOperation(
    val op: String,
    val path: String,
    val value: Any,
)
