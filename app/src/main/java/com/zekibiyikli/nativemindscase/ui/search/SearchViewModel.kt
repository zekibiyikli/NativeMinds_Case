package com.zekibiyikli.nativemindscase.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsEvent
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsParam
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsSource
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.data.content.ContentRepository
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.content.model.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Bos arama ekrani: kategori kesfi + one cikan bir kategoriden ornekler.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    contentRepository: ContentRepository
) : ViewModel() {

    /**
     * Etiket uzunluguna gore siralaniyor: akan izgarada kisa ve uzun cipler
     * karisik gelince satir sonlarinda genis bosluklar kaliyor, uzunluga gore
     * gruplandiginda satirlar daha dolu gorunuyor.
     *
     * Anasayfadaki cip seridi bu siralamayi kullanmiyor; orada kategorilerin
     * tanimlandigi sira korunuyor.
     */
    val subjects: List<Subject> = contentRepository.subjects()
        .sortedBy { it.name.length }

    /**
     * Her ekran acilisinda populer kategorilerden biri secilir; boylece
     * kullanici her gelisinde farkli bir sey gorur. ViewModel omru boyunca
     * sabit kalir, yeniden kompozisyonda degismez.
     */
    val featuredSubject: Subject = contentRepository.popularSubjects().random()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val featuredItems: StateFlow<Outcome<List<ContentItem>>> =
        contentRepository.search(query = null, subjectId = featuredSubject.id)
            .map { outcome ->
                // Serit yatay; tum sayfayi degil bir kismini gostermek yeterli.
                if (outcome is Outcome.Success) {
                    Outcome.Success(outcome.data.take(AppConfig.Ui.FEATURED_ROW_ITEM_COUNT))
                } else {
                    outcome
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS),
                initialValue = Outcome.Loading
            )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    /** Cip tiklamasi navigasyonu tetikliyor; event de buradan gidiyor. */
    fun onSubjectSelected(subjectId: String) {
        AnalyticsHelper.logEvent(
            name = AnalyticsEvent.SELECT_CATEGORY,
            params = mapOf(
                AnalyticsParam.CATEGORY_ID to subjectId,
                AnalyticsParam.SOURCE to AnalyticsSource.SEARCH
            )
        )
    }
}
