package com.zekibiyikli.nativemindscase.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.zekibiyikli.nativemindscase.ui.components.ContentGrid
import com.zekibiyikli.nativemindscase.ui.components.OutcomeContent

@Composable
fun SearchResultsScreen(
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchResultsViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    TrackScreenView(screenName = "search_results", screenClass = "SearchResultsScreen")

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.safeDrawingPadding()) {
            SearchBarRow(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = {},
                onBackClick = onBackClick
            )

            val heading = when {
                query.isNotBlank() -> stringResource(R.string.search_results_title, query)
                else -> viewModel.subjectName.orEmpty()
            }
            if (heading.isNotBlank()) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            OutcomeContent(outcome = results, onRetry = viewModel::onRetry) { items ->
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.search_results_empty, query),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    ContentGrid(
                        items = items,
                        onItemClick = onItemClick,
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
}
