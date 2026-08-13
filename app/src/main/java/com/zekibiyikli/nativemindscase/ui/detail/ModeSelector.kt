package com.zekibiyikli.nativemindscase.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.enums.DetailMode

private const val SLIDE_DURATION_MS = 280
private val SELECTOR_WIDTH = 240.dp
private val SELECTOR_HEIGHT = 48.dp
private val INDICATOR_INSET = 4.dp

/**
 * Kitap/Sesli secimi. Segmented button yerine hap gorunumlu bir kapsayici;
 * secili tarafi gosteren dolgu iki secenek arasinda kayarak gidiyor.
 */
@Composable
fun ModeSelector(
    mode: DetailMode,
    onModeChange: (DetailMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        DetailMode.BOOK to R.string.detail_mode_book,
        DetailMode.AUDIO to R.string.detail_mode_audio
    )

    // BiasAlignment: -1 basa, +1 sona yaslar. Dolgu kapsayicinin yarisi
    // genisliginde oldugu icin bu iki uc tam olarak iki secenege denk geliyor.
    val indicatorBias by animateFloatAsState(
        targetValue = if (mode == DetailMode.BOOK) -1f else 1f,
        animationSpec = tween(durationMillis = SLIDE_DURATION_MS, easing = FastOutSlowInEasing),
        label = "modeIndicator"
    )

    Box(
        modifier = modifier
            .width(SELECTOR_WIDTH)
            .height(SELECTOR_HEIGHT)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .align(BiasAlignment(horizontalBias = indicatorBias, verticalBias = 0f))
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .padding(INDICATOR_INSET)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEach { (value, labelRes) ->
                val selected = mode == value
                val textColor by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(SLIDE_DURATION_MS),
                    label = "modeLabel"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            // Dalga efekti kayan dolgunun uzerinde gurultu yapiyor.
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onModeChange(value) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}
