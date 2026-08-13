package com.zekibiyikli.nativemindscase.enums

import androidx.annotation.StringRes
import com.zekibiyikli.nativemindscase.R

/**
 * Premium sayfasindaki abonelik secenekleri.
 *
 * Fiyatlar simdilik string kaynagindan geliyor. Play Billing baglandiginda
 * bu degerler ProductDetails'ten okunacak; enum yapisi kalabilir, sadece
 * fiyat alanlari urun ID'siyle eslesecek.
 */
enum class PremiumPlan(
    @StringRes val labelRes: Int,
    @StringRes val priceRes: Int,
    @StringRes val periodRes: Int,
    val isBestOffer: Boolean = false
) {
    WEEKLY(R.string.premium_plan_weekly, R.string.premium_price_weekly, R.string.premium_period_weekly),
    MONTHLY(R.string.premium_plan_monthly, R.string.premium_price_monthly, R.string.premium_period_monthly),
    YEARLY(
        labelRes = R.string.premium_plan_yearly,
        priceRes = R.string.premium_price_yearly,
        periodRes = R.string.premium_period_yearly,
        isBestOffer = true
    );

    /**
     * Analitikte kullanilan ad. Enum adi degistirilirse rapordaki deger de
     * degisir; GA4'te gecmis veriyle kiyaslanacaksa bu isim sabit kalmali.
     */
    val analyticsName: String get() = name.lowercase()
}
