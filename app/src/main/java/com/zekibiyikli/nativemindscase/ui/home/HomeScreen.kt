package com.zekibiyikli.nativemindscase.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.analytics.TrackScreenView
import com.zekibiyikli.nativemindscase.data.content.model.Subject
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsSource
import com.zekibiyikli.nativemindscase.ui.components.PagedContentGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onPremiumClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val feed = viewModel.feed.collectAsLazyPagingItems()

    // itemCount kosulu onemli: ilk acilista da refresh durumu Loading olur,
    // o an cekme gostergesi degil tam ekran spinner gorunmeli.
    val isRefreshing = feed.loadState.refresh is LoadState.Loading && feed.itemCount > 0

    TrackScreenView(screenName = "home", screenClass = "HomeScreen")

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Ucretsiz kullanicida kalan gunluk hak; tiklayinca Premium sayfasi.
                    if (!uiState.isPremium) {
                        TextButton(onClick = onPremiumClick) {
                            Text(stringResource(R.string.quota_remaining, uiState.remainingFreeReads))
                        }
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = stringResource(R.string.cd_search)
                        )
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_favorite),
                            contentDescription = stringResource(R.string.cd_favorites)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SubjectChips(
                subjects = uiState.subjects,
                selectedSubjectId = uiState.selectedSubjectId,
                onSubjectClick = viewModel::onSubjectSelected
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = feed::refresh,
                // Ciplerin altinda kalan alani kaplasin; cekme jesti
                // izgaranin nested scroll'undan geliyor.
                modifier = Modifier.weight(1f)
            ) {
                PagedContentGrid(
                    items = feed,
                    onItemClick = onItemClick,
                    source = AnalyticsSource.HOME,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 24.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun SubjectChips(
    subjects: List<Subject>,
    selectedSubjectId: String,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Secim degistiginde secili cip goruse ortalanir; kategori sayisi
    // fazla oldugu icin secili olan ekran disinda kalabiliyor.
    LaunchedEffect(selectedSubjectId, subjects) {
        val index = subjects.indexOfFirst { it.id == selectedSubjectId }
        if (index >= 0) listState.animateScrollToCenter(index)
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = subjects, key = { it.id }) { subject ->
            FilterChip(
                selected = subject.id == selectedSubjectId,
                onClick = { onSubjectClick(subject.id) },
                label = { Text(subject.name) }
            )
        }
    }
}

/**
 * [index] numarali ogeyi goruntu alaninin ortasina kaydirir.
 *
 * Oge ekranda degilse genisligi bilinmedigi icin once ona atlanir,
 * sonra olculen konuma gore ince ayar yapilir.
 */
private suspend fun LazyListState.animateScrollToCenter(index: Int) {
    if (layoutInfo.visibleItemsInfo.none { it.index == index }) {
        animateScrollToItem(index)
    }

    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    val itemCenter = itemInfo.offset + itemInfo.size / 2

    // Listenin basinda/sonunda kaydirma zaten sinira dayanir, ekstra kontrol gerekmiyor.
    animateScrollBy((itemCenter - viewportCenter).toFloat())
}
