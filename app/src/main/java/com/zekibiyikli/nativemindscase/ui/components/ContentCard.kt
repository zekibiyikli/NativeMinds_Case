package com.zekibiyikli.nativemindscase.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem

/**
 * Kapak gorseli; baslik gorselin en altina, karartma gecisinin uzerine biniyor.
 * Feed, arama sonuclari ve favorilerde ortak kullaniliyor.
 */
@Composable
fun ContentCard(
    item: ContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(AppConfig.Ui.COVER_ASPECT_RATIO)
            // clip once: karartma ve gorsel kosede tasmasin.
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model = item.coverUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            // Ag yokken de duzgun gorunsun diye placeholder/error ayni.
            // Baslik zaten ustte yazildigi icin fallback'te tekrar edilmiyor.
            loading = { CoverFallback(item, showTitle = false) },
            error = { CoverFallback(item, showTitle = false) },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(ScrimBrush)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                // Karartma uzerinde her kapakta okunur olmasi icin sabit beyaz.
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Yazinin arkasindaki karartma. Ustte tamamen seffaf baslayip asagi
 * dogru koyulasiyor, boylece gorselden metne gecis sert olmuyor.
 */
private val ScrimBrush = Brush.verticalGradient(
    0f to Color.Transparent,
    0.45f to Color.Black.copy(alpha = 0.18f),
    1f to Color.Black.copy(alpha = 0.55f)
)

/**
 * Gorsel gelmeden once/gelmediginde gosterilen kapak. Renk id'den
 * turetildigi icin ayni icerik her zaman ayni tonu aliyor.
 *
 * [showTitle], baslik baska bir katmanda zaten yaziliyorsa kapatilir.
 */
@Composable
fun CoverFallback(
    item: ContentItem,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    val hue = (item.id.hashCode().rem(360).let { if (it < 0) it + 360 else it }).toFloat()
    val top = Color.hsl(hue, saturation = 0.45f, lightness = 0.55f)
    val bottom = Color.hsl((hue + 28f) % 360f, saturation = 0.5f, lightness = 0.35f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom))),
        contentAlignment = Alignment.BottomStart
    ) {
        if (showTitle) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/** Tasarimlardaki 3 kolonlu icerik izgarasi. */
@Composable
fun ContentGrid(
    items: List<ContentItem>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(AppConfig.Ui.GRID_COLUMNS),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(items = items, key = { it.id }) { item ->
            ContentCard(item = item, onClick = { onItemClick(item.id) })
        }
    }
}
