package com.zekibiyikli.nativemindscase.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.ui.components.CoverFallback

/**
 * Kitabin kunye bilgileri. Okuma gorunumunden cikarilip buraya tasindi;
 * metin ekrani sadece icerige odaklaniyor.
 */
@Composable
fun BookInfoSheet(
    item: ContentItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Kucuk ekranlarda kapak + kunye tasabiliyor.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SubcomposeAsyncImage(
            model = item.coverUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            loading = { CoverFallback(item) },
            error = { CoverFallback(item) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
                .width(140.dp)
                .aspectRatio(AppConfig.Ui.COVER_ASPECT_RATIO)
                .clip(RoundedCornerShape(12.dp))
        )

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        InfoRow(
            label = stringResource(R.string.info_author),
            value = item.author.ifBlank { stringResource(R.string.unknown_author) }
        )
        InfoRow(
            label = stringResource(R.string.info_published),
            value = item.publishedDate ?: stringResource(R.string.info_unknown)
        )
        InfoRow(
            label = stringResource(R.string.info_pages),
            value = item.pageCount
                .takeIf { it > 0 }
                ?.let { stringResource(R.string.detail_page_count, it) }
                ?: stringResource(R.string.info_unknown)
        )
        InfoRow(
            label = stringResource(R.string.info_categories),
            value = item.categories
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
                ?: stringResource(R.string.info_unknown)
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Sabit genislik: degerler alt alta hizali dursun.
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
