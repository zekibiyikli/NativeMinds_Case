package com.zekibiyikli.nativemindscase.core.crashlytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys

/**
 * Firebase Crashlytics icin tek giris noktasi.
 * Yakalanmayan crash'ler otomatik raporlanir; buradaki API'ler
 * yakalanmis hatalar ve crash'e eslik eden kontekst icindir.
 */
object CrashReporter {

    private val crashlytics: FirebaseCrashlytics by lazy { Firebase.crashlytics }

    /** Crash raporuna eklenecek breadcrumb log'u. */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /** Yakalanmis (non-fatal) hatayi raporlar. */
    fun recordException(throwable: Throwable, customKeys: Map<String, String> = emptyMap()) {
        if (customKeys.isNotEmpty()) {
            crashlytics.setCustomKeys {
                customKeys.forEach { (key, value) -> key(key, value) }
            }
        }
        crashlytics.recordException(throwable)
    }

    fun setUserId(userId: String?) {
        crashlytics.setUserId(userId.orEmpty())
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKeys { key(key, value) }
    }

    /** Kullanici onayina bagli raporlama icin. */
    fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }
}
