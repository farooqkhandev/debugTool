package com.quadlogixs.debugtool.api

sealed class ResponseStates<out R> {
    data class Success<out T>(val httpCode: Int, val data: T) : ResponseStates<T>()
    data class Failure(val httpCode: Int, val error: String) : ResponseStates<Nothing>()
    data class Unauthorized(val httpCode: Int, val error: String) : ResponseStates<Nothing>()
    data object Loading : ResponseStates<Nothing>()
    data object Idle : ResponseStates<Nothing>()
}
