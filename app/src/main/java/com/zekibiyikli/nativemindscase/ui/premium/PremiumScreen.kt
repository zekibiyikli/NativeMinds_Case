package com.zekibiyikli.nativemindscase.ui.premium

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.zekibiyikli.nativemindscase.R
import com.zekibiyikli.nativemindscase.core.analytics.TrackScreenView
import com.zekibiyikli.nativemindscase.enums.PremiumPlan

/**
 * Tam ekran satis sayfasi: ustte kenardan kenara animasyonlu header, altinda
 * faydalar, plan secimi ve tek bir aksiyon butonu.
 *
 * TopAppBar yerine header uzerinde bir kapatma butonu var; gorsel durum
 * cubugunun altina uzaniyor, bu yuzden Scaffold kullanilmiyor.
 */
@Composable
fun PremiumScreen(
    onBackClick: () -> Unit,
    onPurchased: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val selectedPlan by viewModel.selectedPlan.collectAsStateWithLifecycle()

    TrackScreenView(screenName = "premium", screenClass = "PremiumScreen")

    // Header koyu bir gorselin altina uzaniyor; acik temada sistem simgeleri de
    // koyu geldigi icin durum cubugu okunmuyordu. Sadece bu ekranda acik
    // simgelere zorlanip cikista onceki degere donuluyor.
    val activity = LocalActivity.current
    val view = LocalView.current
    DisposableEffect(activity, view) {
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            if (previous != null) controller.isAppearanceLightStatusBars = previous
        }
    }

    LaunchedEffect(Unit) {
        viewModel.purchaseCompleted.collect { onPurchased() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        PremiumHeader(onCloseClick = onBackClick)

        // Kalan bosluk bu bloga birakiliyor: paketler alta dayanirken faydalar
        // aradaki alani doldurup ortalaniyor, sigmadiginda kendi icinde kayiyor.
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Kaydirilan icerik sinirsiz yukseklik aliyor; en az bir ekran
            // yuksekliginde tutulmazsa "ortala" dagitacak bosluk bulamiyor.
            val viewportHeight = maxHeight

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = viewportHeight)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically)
            ) {
                Benefit(R.drawable.ic_all_inclusive, R.string.premium_benefit_unlimited)
                Benefit(R.drawable.ic_headphones, R.string.premium_benefit_audio)
                Benefit(R.drawable.ic_download, R.string.premium_benefit_offline)
                Benefit(R.drawable.ic_star, R.string.premium_benefit_early_access)
                Benefit(R.drawable.ic_devices, R.string.premium_benefit_sync)
            }
        }

        if (isPremium) {
            Text(
                text = stringResource(R.string.premium_active),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        } else {
            PlanSelector(
                selectedPlan = selectedPlan,
                onPlanSelect = viewModel::onPlanSelect,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            ContinueButton(
                onClick = viewModel::onPurchaseClick,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            Footer(modifier = Modifier.padding(horizontal = 24.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PremiumHeader(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Gradient icinde okundugu icin composable govdesinde alinmali.
    val surface = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HEADER_HEIGHT_DP.dp)
    ) {
        AsyncImage(
            model = HEADER_GIF_URI,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Ustte kapatma butonu, altta baslik icin okunabilirlik; en altta da
        // sayfa arka planina eriyip kesik bir kenar birakmamasi icin.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.40f),
                        0.30f to Color.Transparent,
                        0.62f to Color.Black.copy(alpha = 0.50f),
                        0.88f to Color.Black.copy(alpha = 0.80f),
                        1f to surface
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color.White
            )
            IconButton(onClick = onCloseClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.cd_close),
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
        ) {
            Text(
                text = stringResource(R.string.premium_header_overline),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.premium_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun Benefit(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PlanSelector(
    selectedPlan: PremiumPlan,
    onPlanSelect: (PremiumPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        // "Best Offer" rozeti kartin ustune tastigi icin ust bosluk birakiliyor.
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumPlan.entries.forEach { plan ->
            PlanCard(
                plan = plan,
                selected = plan == selectedPlan,
                onClick = { onPlanSelect(plan) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: PremiumPlan,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                )
                .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = shape)
                .clickable(onClick = onClick)
                .padding(vertical = 18.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(plan.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(plan.priceRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = stringResource(plan.periodRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        if (plan.isBestOffer) {
            Text(
                text = stringResource(R.string.premium_best_offer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun ContinueButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val primary = MaterialTheme.colorScheme.primary

    Button(
        onClick = onClick,
        shape = shape,
        // Gradient'i Box ciziyor; butonun kendi zemini seffaf birakiliyor.
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.horizontalGradient(
                        listOf(primary.copy(alpha = 0.75f), primary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.premium_cta),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun Footer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.premium_footer_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = listOf(
                stringResource(R.string.premium_privacy_policy),
                stringResource(R.string.premium_restore),
                stringResource(R.string.premium_terms)
            ).joinToString("  |  "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Header gorseli assets altinda; Coil android_asset yolunu dogrudan cozuyor. */
private const val HEADER_GIF_URI = "file:///android_asset/gif_book.gif"

private const val HEADER_HEIGHT_DP = 340
