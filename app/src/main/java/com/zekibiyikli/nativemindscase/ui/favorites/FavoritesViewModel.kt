package com.zekibiyikli.nativemindscase.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zekibiyikli.nativemindscase.data.content.ContentRepository
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    contentRepository: ContentRepository
) : ViewModel() {

    /** cachedIn: konfigurasyon degisiminde sayfalar bastan okunmasin. */
    val favorites: Flow<PagingData<ContentItem>> = contentRepository.pagedFavorites()
        .cachedIn(viewModelScope)
}
