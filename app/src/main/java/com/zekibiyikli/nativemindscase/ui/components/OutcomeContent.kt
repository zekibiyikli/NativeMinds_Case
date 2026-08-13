package com.zekibiyikli.nativemindscase.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zekibiyikli.nativemindscase.core.result.Outcome

/**
 * Ag destekli ekranlarin ortak yukleme/hata/basari iskeleti.
 * Sayfalanan ekranlar bunun yerine [PagedContentGrid] kullaniyor.
 */
@Composable
fun <T> OutcomeContent(
    outcome: Outcome<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    when (outcome) {
        is Outcome.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is Outcome.Failure -> ErrorState(
            throwable = outcome.throwable,
            onRetry = onRetry,
            modifier = modifier
        )

        // modifier ucu durumda da uygulanmali; aksi halde cagiranin verdigi
        // padding (or. Scaffold innerPadding) sadece yukleme/hata ekraninda gecerli olur.
        is Outcome.Success -> Box(modifier = modifier) { content(outcome.data) }
    }
}
