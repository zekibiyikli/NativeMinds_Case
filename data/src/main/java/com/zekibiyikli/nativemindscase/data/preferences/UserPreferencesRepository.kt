package com.zekibiyikli.nativemindscase.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val darkThemeEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val userId: String? = null
)

/**
 * DataStore uzerinden kullanici tercihleri. Okuma tarafi Flow,
 * yazma tarafi suspend — SharedPreferences'in aksine main thread'i bloklamaz.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            // Bozuk dosyada crash yerine varsayilanlara dus.
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { prefs ->
            UserPreferences(
                darkThemeEnabled = prefs[Keys.DARK_THEME] ?: false,
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                userId = prefs[Keys.USER_ID]
            )
        }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setUserId(userId: String?) {
        dataStore.edit { prefs ->
            if (userId == null) prefs.remove(Keys.USER_ID) else prefs[Keys.USER_ID] = userId
        }
    }

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_ID = stringPreferencesKey("user_id")
    }
}
