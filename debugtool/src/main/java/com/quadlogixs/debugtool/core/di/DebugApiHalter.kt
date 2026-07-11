package com.quadlogixs.debugtool.core.di

import com.quadlogixs.debugtool.core.DebugApiHalterChecker
import com.quadlogixs.debugtool.core.network.HaltInterceptResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response

object DebugApiHalter {
    // flip these flags from your debug menu

    // one shared channel for either kind of interception
    private val _pending = MutableStateFlow<Interception?>(null)
    val pending: StateFlow<Interception?> = _pending.asStateFlow()

    // sealed type so we know which dialog to show

    sealed class Interception {
        data class Req(
            val request: Request,
            val originalJson: String,
            val deferred: CompletableDeferred<HaltInterceptResult>
        ) : Interception()

        data class Resp(
            val response: Response,
            val originalJson: String,
            val deferred: CompletableDeferred<String>
        ) : Interception()
    }

    fun interceptRequest(request: Request): HaltInterceptResult? {
        if (DebugApiHalterChecker.haltRequestEnabled || DebugApiHalterChecker.haltAllRequestResponseEnabled) {
            // read body
            val buffer = okio.Buffer().also { request.body?.writeTo(it) }
            val json = buffer.readUtf8()
            // publish
            val deferred = CompletableDeferred<HaltInterceptResult>()
            _pending.value = Interception.Req(request, json, deferred)
            // block until user acts
            val edited = runBlocking { deferred.await() }
            _pending.value = null
            return edited
        } else {
            return null
        }

    }

    fun interceptResponse(response: Response): String? {
        if (DebugApiHalterChecker.haltResponseEnabled || DebugApiHalterChecker.haltAllRequestResponseEnabled) {
            // read body
            val originalJson = response.peekBody(Long.MAX_VALUE).string()
            val deferred = CompletableDeferred<String>()
            _pending.value = Interception.Resp(response, originalJson, deferred)
            val edited = runBlocking { deferred.await() }
            _pending.value = null
            return edited
        } else {
            return null
        }
    }
}

