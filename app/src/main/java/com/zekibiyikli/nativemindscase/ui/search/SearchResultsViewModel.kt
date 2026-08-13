package com.zekibiyikli.nativemindscase.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsEvent
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsParam
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.data.content.ContentRepository
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.navigation.SearchResultsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    contentRepository: ContentRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<SearchResultsRoute>()

    /** Kategori ile gelindiyse baslikta kategori adi gosterilir. */
    val subjectName: String? = route.subjectId
        ?.let { id -> contentRepository.subjects().firstOrNull { it.id == id }?.name }

    private val _query = MutableStateFlow(route.query)
    val query: StateFlow<String> = _query.asStateFlow()

    private val retryTrigger = MutableStateFlow(0)

    val results: StateFlow<Outcome<List<ContentItem>>> =
        combine(_query.debounce(AppConfig.Ui.SEARCH_DEBOUNCE_MS), retryTrigger) { query, _ -> query }
            .flatMapLatest { query ->
                contentRepository.search(query = query, subjectId = route.subjectId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS),
                initialValue = Outcome.Loading
            )

    init {
        /*
         * Event, results akisinin icinde degil ayri bir collector'da: results
         * WhileSubscribed oldugu icin ekran arka plana alinip donduğunde
         * yeniden baslar ve ayni arama tekrar raporlanirdi.
         *
         * debounce: her tusa basista degil kullanici durdugunda gonderilir.
         */
        viewModelScope.launch {
            _query.debounce(AppConfig.Ui.SEARCH_DEBOUNCE_MS)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { term ->
                    AnalyticsHelper.logEvent(
                        name = AnalyticsEvent.SEARCH,
                        params = mapOf(AnalyticsParam.SEARCH_TERM to term)
                    )
                }
        }
    }

    /** Sonuc ekranindaki alan da duzenlenebilir; yazdikca yeni istek gider. */
    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onRetry() {
        retryTrigger.update { it + 1 }
    }
}
