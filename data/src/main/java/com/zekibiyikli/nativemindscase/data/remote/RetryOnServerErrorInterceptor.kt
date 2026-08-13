package com.zekibiyikli.nativemindscase.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import com.zekibiyikli.nativemindscase.core.config.AppConfig

/**
 * Google Books araliklarla 503 donebiliyor; tek seferlik bir aksaklik
 * kullaniciya hata ekrani olarak yansimasin diye kisa araliklarla tekrar dener.
 *
 * Sadece 5xx icin: 4xx'te (gecersiz anahtar, kota) tekrar denemek anlamsiz.
 */
class RetryOnServerErrorInterceptor(
    private val maxAttempts: Int = AppConfig.Network.RETRY_MAX_ATTEMPTS,
    private val backoffMillis: Long = AppConfig.Network.RETRY_BACKOFF_MS,
    private val sleep: (Long) -> Unit = Thread::sleep
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        var attempt = 1

        while (response.code in SERVER_ERROR_RANGE && attempt < maxAttempts) {
            // Yeniden istemeden once govdeyi kapatmak zorunlu, aksi halde baglanti sizar.
            response.close()
            sleep(backoffMillis * attempt)
            attempt++
            response = chain.proceed(chain.request())
        }

        return response
    }

    private companion object {
        val SERVER_ERROR_RANGE = 500..599
    }
}
