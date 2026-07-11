package com.quadlogixs.debugtool.core.network

import com.quadlogixs.debugtool.core.MockApiResponseStore
import com.quadlogixs.debugtool.core.SavedMockResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.SocketTimeoutException

class MockApiResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val mock = MockApiResponseStore.findMatch(
            url = request.url.toString(),
            encodedPath = request.url.encodedPath,
            method = request.method,
        ) ?: return chain.proceed(request)

        if (mock.simulateTimeout) {
            throw SocketTimeoutException("Mock timeout for ${mock.urlPattern}")
        }

        return buildMockResponse(request, mock)
    }

    private fun buildMockResponse(request: okhttp3.Request, mock: SavedMockResponse): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(mock.statusCode)
            .message(statusMessage(mock.statusCode))
            .body(mock.responseBody.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun statusMessage(code: Int): String = when (code) {
        401 -> "Unauthorized"
        500 -> "Internal Server Error"
        504 -> "Gateway Timeout"
        else -> "OK"
    }
}
