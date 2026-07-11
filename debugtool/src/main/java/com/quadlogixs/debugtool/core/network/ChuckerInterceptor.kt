package com.quadlogixs.debugtool.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.quadlogixs.debugtool.api.DebugToolRegistry
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ChuckerInterceptorFactory(
    private val context: Context,
    private val maxContentLength: Long = 550_000L,
) {
    fun createChuckerInterceptor(): ChuckerInterceptor =
        ChuckerInterceptor.Builder(context)
            .collector(createChuckerCollector())
            .maxContentLength(maxContentLength)
            .alwaysReadResponseBody(true)
            .build()

    private fun createChuckerCollector(): ChuckerCollector =
        ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR,
        )
}

class ChuckerInterceptors(
    private val chuckerInterceptorFactory: ChuckerInterceptorFactory,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val chuckerEnabled = DebugToolRegistry.isInstalled() &&
                DebugToolRegistry.config.features.chuckerEnabled
            if (chuckerEnabled) {
                chuckerInterceptorFactory.createChuckerInterceptor().intercept(chain)
            } else {
                chain.proceed(chain.request())
            }
        } catch (e: Exception) {
            throw IOException("Error while processing the request in ChuckerInterceptors", e)
        }
    }
}
