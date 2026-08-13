package com.zekibiyikli.nativemindscase.core.result

/**
 * UI'in anlayacagi hata turleri. Repository, Retrofit/OkHttp istisnalarini
 * buraya cevirir; boylece sunum katmani ag kutuphanelerine bagimli olmaz.
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** Cihazda baglanti yok ya da istek zaman asimina ugradi. */
    class NoConnection(cause: Throwable? = null) : AppException("Baglanti kurulamadi", cause)

    /** HTTP 429 — Google Books kotasi doldu (cogunlukla API anahtari yoklugundan). */
    class RateLimited(cause: Throwable? = null) : AppException("Istek kotasi asildi", cause)

    /**
     * HTTP 400 — Google Books'ta pratikte gecersiz/eksik API anahtari anlamina
     * geliyor; sorgu bicimi kod tarafinda sabit oldugu icin diger 400 nedenleri
     * kullanicinin karsisina cikmiyor.
     */
    class InvalidApiKey(cause: Throwable? = null) : AppException("API anahtari gecersiz", cause)

    class Server(val code: Int, cause: Throwable? = null) : AppException("Sunucu hatasi: $code", cause)

    class Unknown(cause: Throwable? = null) : AppException("Bilinmeyen hata", cause)
}
