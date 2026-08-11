package com.zekibiyikli.nativemindscase.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zekibiyikli.nativemindscase.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.analytics.TrackScreenView
import com.zekibiyikli.nativemindscase.crashlytics.CrashReporter

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrackScreenView(screenName = "home", screenClass = "HomeScreen")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Room'daki kayit sayisi: ${uiState.cachedItemCount}")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Koyu tema (DataStore)")
            Switch(
                checked = uiState.darkThemeEnabled,
                onCheckedChange = viewModel::setDarkTheme
            )
        }

        Button(onClick = viewModel::scheduleSync) {
            Text("Periyodik sync'i planla (WorkManager)")
        }

        HorizontalDivider()

        // Firebase entegrasyonunu konsoldan dogrulamak icin gecici test butonlari.
        Button(
            onClick = {
                AnalyticsHelper.logEvent(
                    name = "test_button_click",
                    params = mapOf("source" to "home", "attempt" to 1)
                )
            }
        ) {
            Text("Test analytics event gonder")
        }

        Button(
            onClick = {
                CrashReporter.log("Test non-fatal butonuna basildi")
                CrashReporter.recordException(
                    IllegalStateException("Test non-fatal"),
                    customKeys = mapOf("screen" to "home")
                )
            }
        ) {
            Text("Test non-fatal gonder")
        }

        Button(
            onClick = {
                CrashReporter.log("Test crash tetikleniyor")
                throw RuntimeException("Test Crash")
            }
        ) {
            Text("Test crash tetikle")
        }
    }
}
