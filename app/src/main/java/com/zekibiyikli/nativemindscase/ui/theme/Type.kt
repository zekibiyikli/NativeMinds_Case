package com.zekibiyikli.nativemindscase.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

/**
 * Detay ekranindaki okuma metni.
 *
 * Serif, basili kitap hissi veriyor ve uzun metinde gozu daha az yoruyor;
 * FontFamily.Serif sistemle geldigi icin ek font dosyasi gerekmiyor.
 * Ozel bir e-kitap fontu (or. Literata) istenirse res/font altina eklenip
 * sadece buradaki fontFamily degistirilir.
 *
 * Satir araligi punto ile birlikte artirildi: 18sp metinde 24sp aralik
 * sikisik duruyordu.
 */
val ReadingTextStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.15.sp
)