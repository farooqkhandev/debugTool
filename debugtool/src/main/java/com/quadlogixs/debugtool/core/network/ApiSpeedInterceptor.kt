package com.quadlogixs.debugtool.core.network

import com.quadlogixs.debugtool.core.ApiSpeedTracker
import okhttp3.Interceptor
import okhttp3.Response

class ApiSpeedInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        var response: Response? = null
        response = try {
            chain.proceed(request)
        } finally {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            ApiSpeedTracker.addRecord(
                url = request.url.toString(),
                method = request.method,
                duration = duration,
                statusCode = response?.code ?: -1
            )
        }
        return response
    }
}


