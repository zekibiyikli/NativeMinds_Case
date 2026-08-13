package com.zekibiyikli.nativemindscase.data.remote.anthropic

import com.zekibiyikli.nativemindscase.core.config.AppConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Claude API kimlik ve surum basliklari.
 *
 * Google Books'un aksine anahtar query parametresi degil baslik olarak gider
 * ve zorunludur — anahtarsiz istek atilmaz (bkz. BookSummaryRepository).
 */
class AnthropicAuthInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", AppConfig.Anthropic.API_VERSION)
            .build()

        return chain.proceed(request)
    }
}
