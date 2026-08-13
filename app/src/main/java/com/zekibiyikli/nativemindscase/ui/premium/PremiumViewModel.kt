package com.zekibiyikli.nativemindscase.ui.premium

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsEvent
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsParam
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsSource
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.premium.PremiumRepository
import com.zekibiyikli.nativemindscase.enums.PremiumPlan
import com.zekibiyikli.nativemindscase.navigation.PremiumRoute
import com.zekibiyikli.nativemindscase.ui.notification.NotificationBannerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val premiumRepository: PremiumRepository,
    private val bannerController: NotificationBannerController
) : ViewModel() {

    /** Kota duvarindan mi gelindi yoksa kullanici kendi mi girdi. */
    private val source = savedStateHandle.toRoute<PremiumRoute>().source

    init {
        AnalyticsHelper.logEvent(
            name = AnalyticsEvent.PREMIUM_OPENED,
            params = mapOf(AnalyticsParam.SOURCE to source)
        )
    }

    val isPremium: StateFlow<Boolean> = premiumRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConfig.Ui.STATE_FLOW_TIMEOUT_MS), initialValue = false)

    private val _purchaseCompleted = MutableSharedFlow<Unit>()
    val purchaseCompleted: SharedFlow<Unit> = _purchaseCompleted.asSharedFlow()

    /** En avantajli plan onceden secili geliyor. */
    private val _selectedPlan = MutableStateFlow(PremiumPlan.YEARLY)
    val selectedPlan: StateFlow<PremiumPlan> = _selectedPlan.asStateFlow()

    fun onPlanSelect(plan: PremiumPlan) {
        if (plan == _selectedPlan.value) return
        _selectedPlan.value = plan

        AnalyticsHelper.logEvent(
            name = AnalyticsEvent.PREMIUM_PLAN_SELECTED,
            params = mapOf(AnalyticsParam.PLAN to plan.analyticsName)
        )
    }

    /**
     * TODO: Play Billing. BillingClient ile urun sorgusu, satin alma akisi ve
     * sunucu tarafi dogrulama buraya gelecek; su an hak dogrudan veriliyor.
     */
    fun onPurchaseClick() {
        viewModelScope.launch {
            premiumRepository.setPremium(enabled = true)
            AnalyticsHelper.logEvent(
                name = AnalyticsEvent.PREMIUM_PURCHASED,
                params = mapOf(
                    AnalyticsParam.SOURCE to AnalyticsSource.PREMIUM_PAGE,
                    AnalyticsParam.PLAN to _selectedPlan.value.analyticsName
                )
            )
            bannerController.success(R.string.banner_premium_active)
            _purchaseCompleted.emit(Unit)
        }
    }
}
