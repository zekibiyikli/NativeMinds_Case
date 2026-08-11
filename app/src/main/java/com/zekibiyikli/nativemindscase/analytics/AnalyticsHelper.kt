package com.zekibiyikli.nativemindscase.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

/**
 * Firebase Analytics icin tek giris noktasi. Uygulamanin geri kalani
 * FirebaseAnalytics API'sine dogrudan bagimli olmasin diye buradan gecer.
 */
object AnalyticsHelper {

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }

    /** Ozel bir event gonderir. Desteklenmeyen tipler string'e cevrilir. */
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        analytics.logEvent(name) {
            params.forEach { (key, value) ->
                when (value) {
                    null -> Unit
                    is String -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Long -> param(key, value)
                    is Float -> param(key, value.toDouble())
                    is Double -> param(key, value)
                    else -> param(key, value.toString())
                }
            }
        }
    }

    /** Ekran goruntulemesi. Compose tarafinda [TrackScreenView] uzerinden cagrilir. */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            buildMap {
                put(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                screenClass?.let { put(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
            }
        )
    }

    fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }

    fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(name, value)
    }

    /** KVKK/GDPR onayi gerektiginde toplamayi kapatmak icin. */
    fun setCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }
}
