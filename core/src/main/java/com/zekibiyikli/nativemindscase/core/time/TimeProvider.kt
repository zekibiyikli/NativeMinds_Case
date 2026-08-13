package com.zekibiyikli.nativemindscase.core.time

import java.util.TimeZone
import javax.inject.Inject

/**
 * Zamani enjekte edilebilir yapar. Gunluk kota gibi tarih siniri olan
 * mantiklar testte sahte saatle dogrulanabilsin diye.
 */
interface TimeProvider {

    fun nowMillis(): Long

    /**
     * Yerel saat dilimine gore gun numarasi. Kota sifirlamasi UTC'ye gore degil,
     * kullanicinin gecesine gore olmali.
     */
    fun currentEpochDay(): Long {
        val now = nowMillis()
        return (now + TimeZone.getDefault().getOffset(now)) / MILLIS_PER_DAY
    }

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
    }
}

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
