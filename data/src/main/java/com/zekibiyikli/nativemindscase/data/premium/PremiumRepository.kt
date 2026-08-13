package com.zekibiyikli.nativemindscase.data.premium

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.zekibiyikli.nativemindscase.core.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.zekibiyikli.nativemindscase.core.config.AppConfig

/**
 * Diyagramdaki "premium mi / gunluk limit asildi mi" kapisi.
 *
 * Gunluk kota, okunan icerik ID'lerinin kumesi olarak tutuluyor; boylece
 * ayni icerigi tekrar acmak kotadan dusmuyor.
 */
@Singleton
class PremiumRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val timeProvider: TimeProvider
) {

    private val preferences: Flow<Preferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }

    val isPremium: Flow<Boolean> = preferences.map { it[Keys.IS_PREMIUM] == true }

    /**
     * Bugun kalan ucretsiz okuma hakki. Gun degisimi yeni bir emisyon
     * gerektirir; uygulama acikken gece yarisini gecmek anlik yansimaz.
     */
    val remainingFreeReads: Flow<Int> = preferences.map { prefs ->
        (AppConfig.Premium.FREE_DAILY_LIMIT - prefs.readItemsToday().size).coerceAtLeast(0)
    }

    /**
     * Bir icerigi acma girisimini kaydeder ve sonucunu doner.
     * Kota dusumu ile kontrol ayni [edit] blogunda oldugu icin atomik.
     */
    suspend fun registerRead(itemId: String): ReadAccess {
        if (isPremium.first()) return ReadAccess.PREMIUM

        var result = ReadAccess.DENIED
        dataStore.edit { prefs ->
            val readToday = prefs.readItemsToday()
            val updated = when {
                // Ayni icerik: zaten sayilmis, tekrar dusme.
                itemId in readToday -> readToday.also { result = ReadAccess.ALREADY_READ }
                readToday.size < AppConfig.Premium.FREE_DAILY_LIMIT ->
                    (readToday + itemId).also { result = ReadAccess.QUOTA_CONSUMED }

                else -> readToday.also { result = ReadAccess.DENIED }
            }
            prefs[Keys.QUOTA_DAY] = timeProvider.currentEpochDay()
            prefs[Keys.READ_ITEMS_TODAY] = updated
        }
        return result
    }

    /**
     * Su an sadece hak bayragini set ediyor; Play Billing akisi
     * dogrulanmis satin alma sonrasi burayi cagiracak.
     */
    suspend fun setPremium(enabled: Boolean) {
        dataStore.edit { it[Keys.IS_PREMIUM] = enabled }
    }

    /** Gun degistiyse dunun kaydi bugune tasinmaz. */
    private fun Preferences.readItemsToday(): Set<String> =
        if (this[Keys.QUOTA_DAY] == timeProvider.currentEpochDay()) {
            this[Keys.READ_ITEMS_TODAY].orEmpty()
        } else {
            emptySet()
        }

    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val QUOTA_DAY = longPreferencesKey("quota_day")
        val READ_ITEMS_TODAY = stringSetPreferencesKey("read_items_today")
    }
}
