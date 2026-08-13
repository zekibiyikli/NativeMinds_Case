package com.zekibiyikli.nativemindscase.data.summary

import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import kotlinx.coroutines.flow.Flow

/**
 * Kitap adindan Ingilizce ozet uretir ve sonucu onbelleklerde saklar.
 */
interface BookSummaryRepository {

    /**
     * Ozet akisi.
     *
     * [Outcome.Success] icindeki deger null ise gosterilecek bir ozet yok —
     * API anahtari tanimli degil, model kitabi tanimiyor ya da istek reddedildi.
     * Cagiran bu durumda Google Books'un kendi aciklamasina dusmeli.
     */
    fun observeSummary(item: ContentItem): Flow<Outcome<String?>>

    /**
     * API anahtari tanimli mi. false ise hic istek atilmaz — repoyu anahtarsiz
     * klonlayan biri de uygulamayi calistirabilsin diye.
     */
    val isEnabled: Boolean
}
