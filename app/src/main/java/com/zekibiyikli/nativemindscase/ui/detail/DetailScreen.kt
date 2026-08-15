package com.zekibiyikli.nativemindscase.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.analytics.TrackScreenView
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.core.result.dataOrNull
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.enums.AccessState
import com.zekibiyikli.nativemindscase.enums.DetailMode
import com.zekibiyikli.nativemindscase.enums.NarrationState
import com.zekibiyikli.nativemindscase.ui.components.CoverFallback
import com.zekibiyikli.nativemindscase.ui.components.OutcomeContent
import com.zekibiyikli.nativemindscase.ui.theme.ReadingTextStyle


private const val ENTER_DURATION_MS = 260
private const val EXIT_DURATION_MS = 180
/** Kayma mesafesi genisligin bu kesri kadar; tam ekran kaymasi sert duruyor. */
private const val SLIDE_FRACTION = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    onQuotaExceeded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val item by viewModel.item.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val access by viewModel.access.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    var showInfoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    TrackScreenView(screenName = "detail", screenClass = "DetailScreen")

    // Ekran gorunmez oldugunda ses arka planda devam etmesin.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onPauseNarration() }

    // Gunluk limit asildiysa icerik hic gosterilmeden Premium sayfasina gidilir.
    LaunchedEffect(access) {
        if (access == AccessState.DENIED) onQuotaExceeded()
    }

    val currentItem = item.dataOrNull()

    if (showInfoSheet && currentItem != null) {
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            sheetState = sheetState
        ) {
            BookInfoSheet(item = currentItem)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentItem?.title.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::onToggleFavorite,
                        // Kitap yuklenmeden favoriye eklenecek bir sey yok.
                        enabled = currentItem != null
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                            ),
                            contentDescription = stringResource(
                                if (isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite
                            )
                        )
                    }
                    IconButton(
                        onClick = { showInfoSheet = true },
                        enabled = currentItem != null
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = stringResource(R.string.cd_book_info)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                ModeSelector(mode = mode, onModeChange = viewModel::onModeChange)
            }
        }
    ) { innerPadding ->
        // Kota kontrolu bitene kadar icerigi sizdirmadan bekle.
        if (access != AccessState.GRANTED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        OutcomeContent(
            outcome = item,
            onRetry = viewModel::onRetry,
            modifier = Modifier.padding(innerPadding)
        ) { current ->
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    // Sesli'ye gecerken yeni icerik sagdan, Kitap'a donerken soldan gelir.
                    val forward = targetState == DetailMode.AUDIO
                    val enterOffset: (Int) -> Int = { width ->
                        if (forward) width / SLIDE_FRACTION else -width / SLIDE_FRACTION
                    }
                    (fadeIn(tween(ENTER_DURATION_MS)) + slideInHorizontally(
                        animationSpec = tween(ENTER_DURATION_MS),
                        initialOffsetX = enterOffset
                    )) togetherWith
                        (fadeOut(tween(EXIT_DURATION_MS)) + slideOutHorizontally(
                            animationSpec = tween(EXIT_DURATION_MS),
                            targetOffsetX = { width -> -enterOffset(width) }
                        ))
                },
                label = "detailMode"
            ) { targetMode ->
                when (targetMode) {
                    DetailMode.BOOK -> BookContent(
                        item = current,
                        summary = summary,
                        isPremium = isPremium
                    )
                    // Seslendirilen metin, ekranda gosterilenle ayni olmali.
                    DetailMode.AUDIO -> AudioContent(
                        item = current,
                        narrationText = readingText(
                            item = current,
                            summary = summary,
                            isPremium = isPremium
                        ),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

/**
 * Okuma gorunumunde ve seslendirmede kullanilan metin.
 *
 * Premium uyede yapay zeka ozeti, ucretsiz kullanicida Google Books'un kendi
 * aciklamasi. Ozet henuz hazir degilse null doner.
 */
private fun readingText(
    item: ContentItem,
    summary: Outcome<String?>,
    isPremium: Boolean
): String? = if (isPremium) {
    summary.dataOrNull()
} else {
    item.description.ifBlank { null }
}

/**
 * Detail (Text).
 *
 * Premium uye yapay zeka ozetini goruyor; ucretsiz kullaniciya Google Books
 * aciklamasi gosteriliyor, o durumda modele hic istek atilmiyor.
 */
@Composable
private fun BookContent(
    item: ContentItem,
    summary: Outcome<String?>,
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        if (!isPremium) {
            Text(
                text = item.description.ifBlank { stringResource(R.string.detail_no_description) },
                style = ReadingTextStyle
            )
        } else {
            Crossfade(targetState = summary, label = "summary") { state ->
                if (state is Outcome.Loading) {
                    SummaryProgress()
                } else {
                    val text = state.dataOrNull()
                    Column {
                        Text(
                            text = text ?: stringResource(R.string.detail_no_summary),
                            style = ReadingTextStyle
                        )

                        // Uretilmis metni kaynakli metinden ayirt edebilmek icin.
                        if (text != null) {
                            Text(
                                text = stringResource(R.string.detail_summary_ai_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryProgress(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp
        )
        Text(
            text = stringResource(R.string.detail_summary_generating),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Detail (Audio): ozeti cihazin TTS motoruyla okur.
 *
 * Onceden bilinen bir sure yok, bu yuzden ilerleme saniye degil cumle
 * uzerinden: slider cumleler arasinda gezinir, yan butonlar bir cumle
 * geri/ileri alir.
 */
@Composable
private fun AudioContent(
    item: ContentItem,
    narrationText: String?,
    viewModel: DetailViewModel,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val narration by viewModel.narrationState.collectAsStateWithLifecycle()
    val sentenceIndex by viewModel.sentenceIndex.collectAsStateWithLifecycle()
    val sentenceCount by viewModel.sentenceCount.collectAsStateWithLifecycle()

    LaunchedEffect(narrationText) { viewModel.onNarrationTextChanged(narrationText) }

    val canPlay = narrationText != null &&
        sentenceCount > 0 &&
        narration == NarrationState.READY

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SubcomposeAsyncImage(
            model = item.coverUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            loading = { CoverFallback(item) },
            error = { CoverFallback(item) },
            modifier = Modifier
                .padding(top = 16.dp)
                .width(220.dp)
                .aspectRatio(AppConfig.Ui.COVER_ASPECT_RATIO)
                .clip(RoundedCornerShape(16.dp))
        )

        Text(
            text = item.author.ifBlank { stringResource(R.string.unknown_author) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Slider(
            value = sentenceIndex.toFloat(),
            onValueChange = { viewModel.onSeekSentence(it.toInt()) },
            // Tek cumlelik metinde kaydiracak bir sey yok; aralik gecerli kalsin diye 1.
            valueRange = 0f..(sentenceCount - 1).coerceAtLeast(1).toFloat(),
            steps = (sentenceCount - 2).coerceAtLeast(0),
            enabled = canPlay && sentenceCount > 1,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = if (sentenceCount > 0) {
                stringResource(R.string.detail_sentence_progress, sentenceIndex + 1, sentenceCount)
            } else {
                ""
            },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { viewModel.onSkipSentence(-1) },
                enabled = canPlay
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_skip_previous),
                    contentDescription = stringResource(R.string.cd_skip_previous)
                )
            }
            FilledIconButton(
                onClick = viewModel::onPlayPause,
                enabled = canPlay,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                    ),
                    contentDescription = stringResource(
                        if (isPlaying) R.string.cd_pause else R.string.cd_play
                    )
                )
            }
            IconButton(
                onClick = { viewModel.onSkipSentence(1) },
                enabled = canPlay
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_skip_next),
                    contentDescription = stringResource(R.string.cd_skip_next)
                )
            }
        }

        // Calisamama sebebi kullaniciya soylenmezse butonlar bozuk gorunuyor.
        val blockedMessage = when {
            narrationText == null -> R.string.detail_audio_no_text
            narration == NarrationState.UNAVAILABLE -> R.string.detail_audio_unavailable
            else -> null
        }
        if (blockedMessage != null) {
            Text(
                text = stringResource(blockedMessage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}