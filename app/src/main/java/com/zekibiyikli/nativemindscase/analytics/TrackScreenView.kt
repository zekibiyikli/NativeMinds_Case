package com.zekibiyikli.nativemindscase.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Bir composable ekrana girildiginde tek sefer screen_view event'i gonderir.
 */
@Composable
fun TrackScreenView(screenName: String, screenClass: String? = null) {
    LaunchedEffect(screenName) {
        AnalyticsHelper.logScreenView(screenName, screenClass)
    }
}
