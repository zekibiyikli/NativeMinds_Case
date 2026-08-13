package com.zekibiyikli.nativemindscase.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.enums.BannerType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * [NotificationBannerController]'a birakilan mesajlari dinleyip gosterir.
 * En ustte, diger icerigin uzerinde durmali; MainActivity'de NavHost'un
 * uzerine yerlestiriliyor.
 */
@Composable
fun TopNotificationBannerHost(
    controller: NotificationBannerController,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf<BannerMessage?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(controller) {
        // collectLatest: yeni mesaj gelirse onceki bekleme iptal olur,
        // banner kapanmadan yeni metne gecer.
        controller.messages.collectLatest { incoming ->
            message = incoming
            visible = true
            delay(AppConfig.Ui.BANNER_VISIBLE_DURATION_MS)
            visible = false
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = visible,
            // Telefonun en ustunden asagi kayar.
            enter = slideInVertically(
                animationSpec = tween(AppConfig.Ui.BANNER_SLIDE_DURATION_MS),
                initialOffsetY = { fullHeight -> -fullHeight }
            ) + fadeIn(tween(AppConfig.Ui.BANNER_SLIDE_DURATION_MS)),
            exit = slideOutVertically(
                animationSpec = tween(AppConfig.Ui.BANNER_SLIDE_DURATION_MS),
                targetOffsetY = { fullHeight -> -fullHeight }
            ) + fadeOut(tween(AppConfig.Ui.BANNER_SLIDE_DURATION_MS))
        ) {
            message?.let { TopNotificationBanner(message = it) }
        }
    }
}

@Composable
private fun TopNotificationBanner(
    message: BannerMessage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Renk status bar'in arkasini da doldursun; yazi asagida kalsin.
            .background(message.type.backgroundColor())
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(message.textRes),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Koyu tonlar secildi: her ikisinde de beyaz yazi ile kontrast
 * yeterli oldugu icin acik/koyu temada ayrisma gerekmiyor.
 */
private fun BannerType.backgroundColor(): Color = when (this) {
    BannerType.SUCCESS -> Color(0xFF2E7D32)
    BannerType.ERROR -> Color(0xFFC62828)
    BannerType.WARNING -> Color(0xFFEF6C00)
    BannerType.INFO -> Color(0xFF1565C0)
}
