package com.quadlogixs.debugtool.api

import android.content.Context
import okhttp3.Interceptor

fun interface DebugNetworkHooks {
    fun contribute(interceptors: MutableList<Interceptor>, context: Context)
}
