package com.quadlogixs.debugtool.core.data

import com.quadlogixs.debugtool.api.AzureDevOpsConfig
import com.quadlogixs.debugtool.api.DebugToolRegistry
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object AzureDevOpsClientFactory {

    fun createApi(config: AzureDevOpsConfig): AzureDevOpsApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                },
            )
            .build()

        return Retrofit.Builder()
            .baseUrl("https://dev.azure.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AzureDevOpsApi::class.java)
    }

    fun authHeader(): String {
        val pat = DebugToolRegistry.config.azure.patProvider.getPat()
        val token = android.util.Base64.encodeToString(
            ":$pat".toByteArray(),
            android.util.Base64.NO_WRAP,
        )
        return "Basic $token"
    }
}
