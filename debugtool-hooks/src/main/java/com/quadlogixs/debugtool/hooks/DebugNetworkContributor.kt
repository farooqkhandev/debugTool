package com.quadlogixs.debugtool.hooks

import android.content.Context
import okhttp3.Interceptor

/**
 * Network contributor installed by the full `:debugtool` library.
 * Host apps should wire OkHttp via [DebugToolNetwork], not this interface.
 */
fun interface DebugNetworkContributor {
    fun contribute(interceptors: MutableList<Interceptor>, context: Context)
}
