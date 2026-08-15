package com.zekibiyikli.nativemindscase.ui.notification

import androidx.annotation.StringRes
import com.zekibiyikli.nativemindscase.enums.BannerType

/**
 * Metin, string resource olarak tasiniyor: ViewModel'ler dile bagimli
 * hale gelmesin, cevrilmis metni Compose tarafi cozsun.
 */
data class BannerMessage(
    @StringRes val textRes: Int,
    val type: BannerType
)
