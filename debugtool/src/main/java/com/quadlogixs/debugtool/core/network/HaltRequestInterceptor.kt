package com.quadlogixs.debugtool.core.network

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody


class HaltInterceptor( private val haltEnableToggle: (isRequest: Boolean) -> Unit,
                       private val haltRequestIntercept: (Request) -> HaltInterceptResult?, private val haltResponseIntercept: (Response) -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 1) possibly halt & edit request
        val originalReq = chain.request()


        val editedReq = haltRequestIntercept(originalReq)

        val reqToUse = if (editedReq != null) {
            val mt = originalReq.body?.contentType() ?: "application/json".toMediaType()

            val builder = originalReq.newBuilder()
                .url(editedReq.url)
                .method(originalReq.method, editedReq.bodyJson.toRequestBody(mt))

            // 🔥 Clear existing headers
            builder.headers(Headers.Builder().build())

            // 🔥 Apply edited headers
            builder.headers(editedReq.headers)
            builder.build()
        } else originalReq


       /* val editedReqJson = haltRequestIntercept(originalReq)
        val reqToUse = if (editedReqJson != null) {
            val mt = originalReq.body?.contentType() ?: "application/json".toMediaType()
            originalReq.newBuilder()
                .url(editedReqJson.url)
                .method(originalReq.method, editedReqJson.bodyJson.toRequestBody(mt))
                .build()
        } else originalReq*/
        // clear flag so only one intercept
        haltEnableToggle(true)

        // 2) proceed
        val originalResp = chain.proceed(reqToUse)

        // 3) possibly halt & edit response
        val editedRespJson = haltResponseIntercept(originalResp)
        val respToUse = if (editedRespJson != null) {
            val mt = originalResp.body?.contentType() ?: "application/json".toMediaType()
            originalResp.newBuilder()
                .body(editedRespJson.toResponseBody(mt))
                .build()
        } else originalResp
        haltEnableToggle(false)

        return respToUse
    }
}

data class HaltInterceptResult(
    val url: String,
    val bodyJson: String,
    val headers: Headers,
)
