package com.zekibiyikli.nativemindscase.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.analytics.TrackScreenView
import com.zekibiyikli.nativemindscase.core.result.Outcome
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.content.model.Subject
import com.zekibiyikli.nativemindscase.ui.components.ContentCard

private val CHIP_SPACING = 4.dp
private val FEATURED_CARD_WIDTH = 104.dp
/** Yukleme gostergesi seridin yerini tutsun, liste gelince ziplama olmasin. */
private val FEATURED_ROW_HEIGHT = 156.dp

@Composable
fun SearchScreen(
    onSearch: (String) -> Unit,
    onSubjectClick: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val featuredItems by viewModel.featuredItems.collectAsStateWithLifecycle()

    TrackScreenView(screenName = "search", screenClass = "SearchScreen")

    Surface(modifier = modifier.fillMaxSize()) {
        // Scaffold kullanmayan ekranlarda edge-to-edge insetlerini elle uyguluyoruz;
        // aksi halde arama alani status bar'in altinda kalip dokunulamaz oluyor.
        Column(modifier = Modifier.safeDrawingPadding()) {
            SearchBarRow(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = { if (query.isNotBlank()) onSearch(query) },
                onBackClick = onBackClick
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SectionTitle(stringResource(R.string.search_new_categories))
                }

                item {
                    SubjectFlowGrid(
                        subjects = viewModel.subjects,
                        onSubjectClick = { subjectId ->
                            viewModel.onSubjectSelected(subjectId)
                            onSubjectClick(subjectId)
                        }
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SectionTitle(
                        stringResource(
                            R.string.search_popular_title,
                            viewModel.featuredSubject.name
                        )
                    )
                }

                item {
                    FeaturedRow(outcome = featuredItems, onItemClick = onItemClick)
                }
            }
        }
    }
}

/**
 * One cikan kategoriden ornek kitaplar. Ikincil bir serit oldugu icin
 * hata durumunda ekrani mesajla doldurmuyoruz; sessizce bos kaliyor.
 */
@Composable
private fun FeaturedRow(
    outcome: Outcome<List<ContentItem>>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (outcome) {
        is Outcome.Loading -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(FEATURED_ROW_HEIGHT),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is Outcome.Failure -> Unit

        is Outcome.Success -> if (outcome.data.isEmpty()) {
            Text(
                text = stringResource(R.string.search_popular_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = outcome.data, key = { it.id }) { item ->
                    ContentCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        modifier = Modifier.width(FEATURED_CARD_WIDTH)
                    )
                }
            }
        }
    }
}

/**
 * Kategoriler sabit kolonlu bir izgaraya degil, genisligi kendi metnine
 * gore belirlenen ciplerin akisina yerlesiyor; satirlar dolunca alt satira
 * tasiyor. Siralama ViewModel'de etiket uzunluguna gore yapiliyor, boylece
 * satirlar daha az bosluk birakarak doluyor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectFlowGrid(
    subjects: List<Subject>,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING),
        verticalArrangement = Arrangement.spacedBy(CHIP_SPACING)
    ) {
        subjects.forEach { subject ->
            SuggestionChip(
                onClick = { onSubjectClick(subject.id) },
                label = { Text(subject.name) }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}