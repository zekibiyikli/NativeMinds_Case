package com.zekibiyikli.nativemindscase.enums

/** Cihazin seslendirme motorunun durumu. */
enum class NarrationState {
    /** Motor baglaniyor; kontroller henuz kullanilamaz. */
    INITIALIZING,

    READY,

    /** Motor yok ya da Ingilizce ses paketi kurulu degil. */
    UNAVAILABLE
}
