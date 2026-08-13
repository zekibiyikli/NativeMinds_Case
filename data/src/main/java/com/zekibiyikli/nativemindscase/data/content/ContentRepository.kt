package com.zekibiyikli.nativemindscase.data.content

import androidx.paging.PagingData
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.content.model.Subject
import kotlinx.coroutines.flow.Flow

/**
 * Icerik kaynagi.
 *
 * Tek dogruluk kaynagi Room: feed ve favoriler yerelden okunur, Google Books
 * yalnizca onbellegi tazeler. Bu sayede internet yokken de son gorulen
 * icerik listelenir. Istisna [search] — sorgu sonuclari onbelleklenmez.
 *
 * Ag'a giden akislar [Outcome] doner cunku yukleme ve hata durumlarinin
 * ekranda karsiligi var; sayfali listeler Paging'in kendi durumunu kullanir.
 */
interface ContentRepository {

    /**
     * Google Books'ta kategori listeleyen endpoint yok; sabit liste
     * uygulama tarafinda tutuluyor.
     */
    fun subjects(): List<Subject>

    /**
     * Anasayfa feed'i. Liste Room'dan okunur; kullanici asagi indikce
     * RemoteMediator yeni sayfayi cekip onbellege yazar.
     */
    fun pagedFeed(subjectId: String): Flow<PagingData<ContentItem>>

    /**
     * Once yerel kopya, sonra ag'dan tazelenmis hali yayinlanir. Yerel kopya
     * varken ag hatasi gosterilmez.
     */
    fun observeItem(id: String): Flow<Outcome<ContentItem>>

    /**
     * [query] ve [subjectId] birlikte verilirse Google Books'un
     * "kelime + subject:" birlesik filtresi kullanilir.
     */
    fun search(query: String?, subjectId: String?): Flow<Outcome<List<ContentItem>>>

    /** [subjects] icinden one cikarilmaya deger olanlar. */
    fun popularSubjects(): List<Subject>

    /** Favoriler ekrani icin sayfali liste. */
    fun pagedFavorites(): Flow<PagingData<ContentItem>>

    fun observeFavoriteIds(): Flow<Set<String>>

    /** Favorideyse cikarir, degilse karti cizmeye yetecek alanlarla kaydeder. */
    suspend fun toggleFavorite(item: ContentItem)

    /** Kayitli favorilerin bilgilerini API'den tazeler (periyodik sync). */
    suspend fun refreshFavorites()
}
