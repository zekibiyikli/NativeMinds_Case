package com.zekibiyikli.nativemindscase.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.zekibiyikli.nativemindscase.data.local.SampleDao
import com.zekibiyikli.nativemindscase.data.preferences.UserPreferencesRepository
import com.zekibiyikli.nativemindscase.work.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val darkThemeEnabled: Boolean = false,
    val cachedItemCount: Int = 0
)

/**
 * Hilt + Room + DataStore + Flow zincirinin uctan uca calistigi ornek ViewModel.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager,
    sampleDao: SampleDao
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesRepository.userPreferences,
        sampleDao.observeAll()
    ) { preferences, items ->
        HomeUiState(
            darkThemeEnabled = preferences.darkThemeEnabled,
            cachedItemCount = items.size
        )
    }.stateIn(
        scope = viewModelScope,
        // Konfigurasyon degisiminde akisi bosuna yeniden baslatmamak icin 5sn tolerans.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkThemeEnabled(enabled)
        }
    }

    fun scheduleSync() {
        SyncWorker.enqueuePeriodic(workManager)
    }
}
