package com.quadlogixs.debugtool.hooks

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Always-safe OkHttp helpers. No-ops when full `:debugtool` has not installed a contributor.
 *
 * Prefer [addDebugToolInterceptors] so Mock / Halt / Chucker / ApiSpeed each register
 * as separate application interceptors.
 */
object DebugToolNetwork {

    private val NO_OP = Interceptor { chain -> chain.proceed(chain.request()) }

    /**
     * Single interceptor that forwards to all installed debug interceptors (or no-op).
     * Prefer [addDebugToolInterceptors] when building the client.
     */
    fun interceptor(context: Context): Interceptor {
        val list = mutableListOf<Interceptor>()
        DebugToolHooks.networkContributorOrNull()?.contribute(list, context)
        return when {
            list.isEmpty() -> NO_OP
            list.size == 1 -> list[0]
            else -> CompositeInterceptor(list)
        }
    }

    fun OkHttpClient.Builder.addDebugToolInterceptors(context: Context): OkHttpClient.Builder {
        val list = mutableListOf<Interceptor>()
        DebugToolHooks.networkContributorOrNull()?.contribute(list, context)
        list.forEach { addInterceptor(it) }
        return this
    }

    private class CompositeInterceptor(
        private val interceptors: List<Interceptor>,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response =
            NextChain(chain, interceptors, 0).proceed(chain.request())

        private class NextChain(
            private val chain: Interceptor.Chain,
            private val interceptors: List<Interceptor>,
            private val index: Int,
        ) : Interceptor.Chain by chain {
            override fun proceed(request: Request): Response {
                if (index >= interceptors.size) {
                    return chain.proceed(request)
                }
                val next = NextChain(chain, interceptors, index + 1)
                return interceptors[index].intercept(
                    object : Interceptor.Chain by next {
                        override fun request(): Request = request
                    },
                )
            }
        }
    }
}
