package com.zekibiyikli.nativemindscase.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsEvent
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsParam
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsSource
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.content.ContentRepository
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.content.model.Subject
import com.zekibiyikli.nativemindscase.data.premium.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Feed [feed] akisinda ayri tasiniyor: Paging kendi yukleme/hata
 * durumunu tasidigi icin [HomeUiState] icine sigmaz.
 */
data class HomeUiState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: String = "",
    val isPremium: Boolean = false,
    val remainingFreeReads: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    contentRepository: ContentRepository,
    premiumRepository: PremiumRepository
) : ViewModel() {

    private val subjects = contentRepository.subjects()

    /** Google Books'ta global feed yok; her zaman bir kategori secili olmali. */
    private val _selectedSubjectId = MutableStateFlow(subjects.first().id)
    val selectedSubjectId: StateFlow<String> = _selectedSubjectId.asStateFlow()

    /**
     * cachedIn: konfigurasyon degisiminde sayfalar bastan cekilmesin.
     * flatMapLatest: kategori degisince onceki sayfalama iptal olur.
     */
    val feed: Flow<PagingData<ContentItem>> = _selectedSubjectId
        .flatMapLatest(contentRepository::pagedFeed)
        .cachedIn(viewModelScope)

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedSubjectId,
        premiumRepository.isPremium,
        premiumRepository.remainingFreeReads
    ) { selectedId, isPremium, remaining ->
        HomeUiState(
            subjects = subjects,
            selectedSubjectId = selectedId,
            isPremium = isPremium,
            remainingFreeReads = remaining
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS),
        initialValue = HomeUiState(subjects = subjects, selectedSubjectId = _selectedSubjectId.value)
    )

    fun onSubjectSelected(subjectId: String) {
        if (subjectId == _selectedSubjectId.value) return
        _selectedSubjectId.value = subjectId

        AnalyticsHelper.logEvent(
            name = AnalyticsEvent.SELECT_CATEGORY,
            params = mapOf(
                AnalyticsParam.CATEGORY_ID to subjectId,
                AnalyticsParam.SOURCE to AnalyticsSource.HOME
            )
        )
    }
}
