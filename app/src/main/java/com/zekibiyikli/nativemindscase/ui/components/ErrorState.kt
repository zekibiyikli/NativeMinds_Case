package com.zekibiyikli.nativemindscase.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.result.AppException

/**
 * Hata turunu kullanicinin ne yapacagini bilecegi bir mesaja cevirir.
 * Tek yerde durmali; hem tam ekran hata hem sayfalama hatasi bunu kullaniyor.
 */

/** Ekranda gosterilecek icerik yokken kullanilan tam ekran hata durumu. */
@Composable
fun ErrorState(
    throwable: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(throwable.messageRes()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@StringRes
fun Throwable.messageRes(): Int = when (this) {
    is AppException.NoConnection -> R.string.error_network
    is AppException.RateLimited -> R.string.error_rate_limited
    is AppException.InvalidApiKey -> R.string.error_invalid_api_key
    else -> R.string.error_generic
}
