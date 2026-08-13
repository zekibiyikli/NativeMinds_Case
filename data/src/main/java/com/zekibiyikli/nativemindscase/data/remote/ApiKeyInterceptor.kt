package com.zekibiyikli.nativemindscase.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * API anahtarini her istege ekler.
 *
 * Google Books anahtarsiz isteklere de cevap veriyor (daha dusuk kota ile),
 * bu yuzden anahtar tanimli degilse istek key parametresi olmadan gider —
 * uygulama local.properties bos olsa da calisir.
 */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (apiKey.isBlank()) return chain.proceed(request)

        val url = request.url.newBuilder()
            .addQueryParameter("key", apiKey)
            .build()

        return chain.proceed(request.newBuilder().url(url).build())
    }
}
