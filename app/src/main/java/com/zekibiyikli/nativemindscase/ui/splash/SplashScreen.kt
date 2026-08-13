package com.zekibiyikli.nativemindscase.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.analytics.TrackScreenView
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Asset zip (dotLottie) oldugu icin Lottie dosyayi acip icindeki
    // animasyonu kendisi cozuyor.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("anim_book.lottie")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    TrackScreenView(screenName = "splash", screenClass = "SplashScreen")

    // Animasyon yuklenemese bile gecis yapilmali; bu yuzden sure
    // animasyonun durumuna degil, sabit gecikmeye bagli.
    LaunchedEffect(Unit) {
        delay(AppConfig.Ui.SPLASH_DURATION_MS)
        onFinished()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(contentAlignment = Alignment.Center) {
            val description = stringResource(R.string.cd_splash_animation)
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(AppConfig.Ui.SPLASH_ANIMATION_SIZE_DP.dp)
                    .semantics { contentDescription = description }
            )
        }
    }
}
