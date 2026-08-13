package com.zekibiyikli.nativemindscase.ui.notification

import androidx.annotation.StringRes
import com.zekibiyikli.nativemindscase.enums.BannerType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Metin, string resource olarak tasiniyor: ViewModel'ler dile bagimli
 * hale gelmesin, cevrilmis metni Compose tarafi cozsun.
 */
data class BannerMessage(
    @StringRes val textRes: Int,
    val type: BannerType
)

/**
 * Uygulama genelinde tek banner sirasi. ViewModel'ler buraya mesaj birakir,
 * [TopNotificationBannerHost] gosterir.
 *
 * SharedFlow kullaniliyor cunku bu bir olay: ayni mesaj arka arkaya
 * gonderildiginde tekrar gorunmeli (StateFlow ayni degeri yutardi).
 */
@Singleton
class NotificationBannerController @Inject constructor() {

    private val _messages = MutableSharedFlow<BannerMessage>(
        extraBufferCapacity = 1,
        // Hizli ard arda mesajda eskisini dusur; kuyruk birikmesin.
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<BannerMessage> = _messages.asSharedFlow()

    fun show(@StringRes textRes: Int, type: BannerType) {
        _messages.tryEmit(BannerMessage(textRes = textRes, type = type))
    }

    fun success(@StringRes textRes: Int) = show(textRes, BannerType.SUCCESS)

    fun error(@StringRes textRes: Int) = show(textRes, BannerType.ERROR)

    fun warning(@StringRes textRes: Int) = show(textRes, BannerType.WARNING)

    fun info(@StringRes textRes: Int) = show(textRes, BannerType.INFO)
}
