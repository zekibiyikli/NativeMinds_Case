package com.zekibiyikli.nativemindscase.data.remote

import com.zekibiyikli.nativemindscase.data.remote.dto.VolumeDto
import com.zekibiyikli.nativemindscase.data.remote.dto.VolumesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.zekibiyikli.nativemindscase.core.config.AppConfig

/**
 * Google Books API v1. API anahtari her istege [ApiKeyInterceptor] tarafindan
 * ekleniyor, bu yuzden burada key parametresi yok.
 */
interface GoogleBooksApi {

    /**
     * Postman koleksiyonundaki 1, 2, 2b ve 4 numarali istekler ayni endpoint;
     * fark sadece [query] ifadesinin nasil kuruldugunda (bkz. SearchQuery).
     */
    @GET("volumes")
    suspend fun searchVolumes(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = AppConfig.Network.PAGE_SIZE,
        @Query("startIndex") startIndex: Int = 0,
        @Query("orderBy") orderBy: String? = null,
        @Query("langRestrict") langRestrict: String? = null
    ): VolumesResponseDto

    /** Detay ekrani: ozet dahil tek kitabin tam bilgisi. */
    @GET("volumes/{volumeId}")
    suspend fun getVolume(@Path("volumeId") volumeId: String): VolumeDto
}

/**
 * q parametresini kuran tek yer. Google Books'ta kategori filtresi
 * ayri bir parametre degil, sorgu ifadesinin parcasi.
 */
object SearchQuery {

    fun build(query: String?, subjectId: String?): String {
        val trimmedQuery = query?.trim().orEmpty()
        val subjectTerm = subjectId?.trim()?.takeIf { it.isNotEmpty() }?.let { subject ->
            // Cok kelimeli kategoriler ("Science Fiction") tirnaklanmali;
            // aksi halde Google ikinci kelimeyi ayri bir arama terimi sayar.
            if (subject.any(Char::isWhitespace)) "subject:\"$subject\"" else "subject:$subject"
        }

        // Koleksiyonda terimler "+" ile ayrilmis; bu ham URL'de bosluk demek.
        // Retrofit @Query degeri yuzde-kodladigi icin literal "+" %2B'ye donusur
        // ve arama terimi bozulur. Bosluk (%20) dogru AND ayiracidir.
        return listOfNotNull(trimmedQuery.takeIf { it.isNotEmpty() }, subjectTerm)
            .joinToString(separator = " ")
    }
}
