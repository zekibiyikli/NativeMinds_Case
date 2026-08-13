package com.zekibiyikli.nativemindscase.ui.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsEvent
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsParam
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.core.result.dataOrNull
import com.zekibiyikli.nativemindscase.data.content.ContentRepository
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.premium.PremiumRepository
import com.zekibiyikli.nativemindscase.data.premium.ReadAccess
import com.zekibiyikli.nativemindscase.data.summary.BookSummaryRepository
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.enums.AccessState
import com.zekibiyikli.nativemindscase.enums.DetailMode
import com.zekibiyikli.nativemindscase.enums.NarrationState
import com.zekibiyikli.nativemindscase.navigation.DetailRoute
import com.zekibiyikli.nativemindscase.speech.BookNarrator
import com.zekibiyikli.nativemindscase.ui.notification.NotificationBannerController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    private val contentRepository: ContentRepository,
    private val premiumRepository: PremiumRepository,
    private val summaryRepository: BookSummaryRepository,
    private val bannerController: NotificationBannerController
) : ViewModel() {

    private val route = savedStateHandle.toRoute<DetailRoute>()
    private val itemId = route.itemId

    private val retryTrigger = MutableStateFlow(0)

    val item: StateFlow<Outcome<ContentItem>> = retryTrigger
        .flatMapLatest { contentRepository.observeItem(itemId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS), Outcome.Loading)

    /** Yapay zeka ozeti premium'a ozel; ucretsiz kullanici aciklamayi goruyor. */
    val isPremium: StateFlow<Boolean> = premiumRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS), initialValue = false)

    /**
     * Kitabin yapay zeka ile uretilmis Ingilizce ozeti.
     *
     * Sadece premium uyede uretilir; ucretsiz kullanicida Claude'a hic istek
     * atilmaz, ekran Google Books aciklamasina duser.
     *
     * Success icindeki deger null ise gosterilecek ozet yok: kullanici premium
     * degil, API anahtari tanimsiz, model kitabi tanimiyor ya da istek
     * reddedildi.
     */
    val summary: StateFlow<Outcome<String?>> = combine(
        // Kitap once yerel kopyadan, sonra ag'dan geliyor. Ayni kitabin ikinci
        // yayini ozet istegini iptal edip bastan baslatmasin.
        item.map { it.dataOrNull() }.distinctUntilChangedBy { it?.id },
        premiumRepository.isPremium
    ) { current, premium -> current to premium }
        .flatMapLatest { (current, premium) ->
            when {
                // Ucretsiz kullanici icin istek atmiyoruz; maliyeti olan bir cagri.
                !premium -> flowOf(Outcome.Success(null))
                // Kitabin kendisi gelmediyse hata zaten ekranda; ozet beklemede kalir.
                current == null -> flowOf(Outcome.Loading)
                else -> summaryRepository.observeSummary(current)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS), Outcome.Loading)

    val isFavorite: StateFlow<Boolean> = contentRepository.observeFavoriteIds()
        .map { itemId in it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS), initialValue = false)

    private val _access = MutableStateFlow(AccessState.CHECKING)
    val access: StateFlow<AccessState> = _access.asStateFlow()

    private val _mode = MutableStateFlow(DetailMode.BOOK)
    val mode: StateFlow<DetailMode> = _mode.asStateFlow()

    /**
     * Sesli modu ozeti cihazin TTS motoruyla okuyor; ilerleme cumle
     * bazinda, cunku onceden bilinen bir sure yok.
     */
    private val narrator = BookNarrator(context)

    val isPlaying: StateFlow<Boolean> = narrator.isSpeaking
    val narrationState: StateFlow<NarrationState> = narrator.state
    val sentenceIndex: StateFlow<Int> = narrator.sentenceIndex
    val sentenceCount: StateFlow<Int> = narrator.sentenceCount

    init {
        // Ekran acilirken kota dusulur; premium kullanicida dokunulmaz.
        viewModelScope.launch {
            val access = premiumRepository.registerRead(itemId)
            when (access) {
                ReadAccess.QUOTA_CONSUMED -> AnalyticsHelper.logEvent(
                    name = AnalyticsEvent.QUOTA_CONSUMED,
                    params = mapOf(
                        AnalyticsParam.REMAINING to premiumRepository.remainingFreeReads.first()
                    )
                )

                ReadAccess.DENIED -> {
                    AnalyticsHelper.logEvent(
                        name = AnalyticsEvent.QUOTA_EXCEEDED,
                        params = mapOf(AnalyticsParam.ITEM_ID to itemId)
                    )
                    bannerController.warning(R.string.banner_quota_exceeded)
                }

                // Premium uye ya da bugun zaten acilmis kitap: hak harcanmadi,
                // "hak kullanildi" diye raporlanmasi yaniltici olurdu.
                ReadAccess.PREMIUM, ReadAccess.ALREADY_READ -> Unit
            }
            _access.value = if (access.isGranted) AccessState.GRANTED else AccessState.DENIED
        }

        // Kitap adi ancak yuklendikten sonra biliniyor; raporda ID yerine
        // baslik gorunsun diye ilk basarili sonuc beklenip tek sefer gonderiliyor.
        viewModelScope.launch {
            val loaded = item.first { it is Outcome.Success }.dataOrNull() ?: return@launch
            AnalyticsHelper.logEvent(
                name = AnalyticsEvent.SELECT_ITEM,
                params = mapOf(
                    AnalyticsParam.ITEM_ID to loaded.id,
                    AnalyticsParam.ITEM_NAME to loaded.title,
                    AnalyticsParam.SOURCE to route.source
                )
            )
        }
    }

    fun onModeChange(mode: DetailMode) {
        if (mode == _mode.value) return

        // Kitap'a donuldugunde ses arka planda devam etmesin.
        if (mode == DetailMode.BOOK) narrator.pause()
        _mode.value = mode

        AnalyticsHelper.logEvent(
            name = AnalyticsEvent.READ_MODE_CHANGE,
            params = mapOf(AnalyticsParam.MODE to mode.name.lowercase())
        )
    }

    fun onRetry() {
        retryTrigger.update { it + 1 }
    }

    fun onToggleFavorite() {
        val current = item.value.dataOrNull() ?: return
        // Durum degisimini toggle'dan once okuyoruz; sonrasinda Flow zaten guncellenmis olur.
        val wasFavorite = isFavorite.value
        viewModelScope.launch {
            contentRepository.toggleFavorite(current)
            bannerController.success(
                if (wasFavorite) R.string.banner_favorite_removed else R.string.banner_favorite_added
            )
        }
    }

    /** Sesli moduna girildiginde okunacak metni hazirlar. */
    fun onNarrationTextChanged(text: String?) {
        if (text.isNullOrBlank()) return
        narrator.setText(text)
    }

    fun onPlayPause() {
        if (isPlaying.value) narrator.pause() else narrator.play()
    }

    /**
     * Uygulama arka plana alindiginda seslendirme durur. Konum korunuyor,
     * geri donuldugunde kalinan cumleden devam edilebiliyor.
     */
    fun onPauseNarration() {
        narrator.pause()
    }

    fun onSeekSentence(index: Int) {
        narrator.seekTo(index)
    }

    fun onSkipSentence(delta: Int) {
        narrator.seekTo(sentenceIndex.value + delta)
    }

    override fun onCleared() {
        super.onCleared()
        narrator.release()
    }
}
