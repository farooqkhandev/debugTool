package com.quadlogixs.debugtool.core

import android.app.Application
import android.content.Context
import com.quadlogixs.debugtool.api.DebugToolConfig
import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.core.di.DebugApiHalter
import com.quadlogixs.debugtool.core.network.ApiSpeedInterceptor
import com.quadlogixs.debugtool.core.network.ChuckerInterceptorFactory
import com.quadlogixs.debugtool.core.network.ChuckerInterceptors
import com.quadlogixs.debugtool.core.network.HaltInterceptor
import com.quadlogixs.debugtool.core.network.MockApiResponseInterceptor
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object DebugToolCore {

    fun init(application: Application, config: DebugToolConfig) {
        DebugToolRegistry.install(config)
        Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler(application))
        if (Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }
        initMockApiResponseStore(application)
    }

    fun contributeNetworkInterceptors(
        interceptors: MutableList<Interceptor>,
        context: Context,
    ) {
        interceptors.add(MockApiResponseInterceptor())
        interceptors.add(
            HaltInterceptor(
                haltEnableToggle = { isRequest ->
                    if (isRequest) {
                        DebugApiHalterChecker.haltRequestEnabled = false
                    } else {
                        DebugApiHalterChecker.haltResponseEnabled = false
                    }
                },
                haltRequestIntercept = { request -> DebugApiHalter.interceptRequest(request) },
                haltResponseIntercept = { response -> DebugApiHalter.interceptResponse(response) },
            ),
        )
        interceptors.add(ChuckerInterceptors(ChuckerInterceptorFactory(context)))
        interceptors.add(ApiSpeedInterceptor())
        interceptors.add(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
    }

    private fun initMockApiResponseStore(context: Context) {
        if (!DebugToolRegistry.config.features.mockApiPersistenceEnabled) return
        val snapshot = MockApiResponsePersistence.load(context)
        MockApiResponseStore.restore(snapshot.globalEnabled, snapshot.mocks)
        MockApiResponseStore.attachPersistence { globalEnabled, mocks ->
            MockApiResponsePersistence.save(
                context,
                MockApiResponseSnapshot(globalEnabled = globalEnabled, mocks = mocks),
            )
        }
    }
}
