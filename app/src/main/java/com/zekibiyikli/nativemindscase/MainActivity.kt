package com.zekibiyikli.nativemindscase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zekibiyikli.nativemindscase.navigation.NativeMindsNavHost
import com.zekibiyikli.nativemindscase.ui.notification.NotificationBannerController
import com.zekibiyikli.nativemindscase.ui.notification.TopNotificationBannerHost
import com.zekibiyikli.nativemindscase.ui.theme.NativeMindsCaseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Banner tum ekranlarin uzerinde tek noktadan gosteriliyor. */
    @Inject lateinit var bannerController: NotificationBannerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NativeMindsCaseTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    NativeMindsNavHost(modifier = Modifier.fillMaxSize())

                    TopNotificationBannerHost(
                        controller = bannerController,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}
