package com.zekibiyikli.nativemindscase.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsEvent
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsHelper
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsParam
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem

/**
 * Sayfalanan icerik izgarasi. Ilk yukleme ve ilk yukleme hatasi tum ekrani
 * kaplar; sonraki sayfalarin durumu izgaranin altinda gosterilir, boylece
 * mevcut icerik ekranda kalir.
 */
@Composable
fun PagedContentGrid(
    items: LazyPagingItems<ContentItem>,
    onItemClick: (String) -> Unit,
    source: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    @StringRes emptyMessageRes: Int = R.string.home_empty
) {
    val refresh = items.loadState.refresh
    // Elimizde zaten kayit varsa tam ekran durumlarina gecmiyoruz; yenileme
    // sirasinda liste ekranda kalmali, yoksa asagi cekme jesti icerigi
    // gozden kaybediyor.
    val isEmpty = items.itemCount == 0

    // Ag hata verdi ama Room'daki kayitlar ekranda: cevrimdisi calismanin
    // gercekten devreye girdigi an. LaunchedEffect icinde, cunku composable
    // govdesinde her yeniden kompozisyonda tekrar gonderilirdi.
    val servedFromCache = refresh is LoadState.Error && !isEmpty
    LaunchedEffect(servedFromCache, source) {
        if (!servedFromCache) return@LaunchedEffect
        AnalyticsHelper.logEvent(
            name = AnalyticsEvent.OFFLINE_CONTENT_SHOWN,
            params = mapOf(
                AnalyticsParam.SOURCE to source,
                AnalyticsParam.ERROR_TYPE to
                    ((refresh as LoadState.Error).error::class.simpleName ?: "unknown")
            )
        )
    }

    when {
        refresh is LoadState.Loading && isEmpty -> FullScreenLoading(modifier)

        refresh is LoadState.Error && isEmpty -> ErrorState(
            throwable = refresh.error,
            onRetry = items::retry,
            modifier = modifier
        )

        refresh is LoadState.NotLoading && isEmpty -> EmptyState(
            messageRes = emptyMessageRes,
            modifier = modifier
        )

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(AppConfig.Ui.GRID_COLUMNS),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier
            ) {
                items(
                    count = items.itemCount,
                    key = items.itemKey { it.id }
                ) { index ->
                    // enablePlaceholders = false oldugu icin null gelmemeli,
                    // yine de Paging sozlesmesi geregi kontrol ediliyor.
                    items[index]?.let { item ->
                        ContentCard(item = item, onClick = { onItemClick(item.id) })
                    }
                }

                // Sonraki sayfanin durumu satirin tamamini kaplasin.
                when (val append = items.loadState.append) {
                    is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                        AppendLoading()
                    }

                    is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                        AppendError(throwable = append.error, onRetry = items::retry)
                    }

                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AppendLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AppendError(
    throwable: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(throwable.messageRes()),
                textAlign = TextAlign.Center
            )
        }
    }
}
