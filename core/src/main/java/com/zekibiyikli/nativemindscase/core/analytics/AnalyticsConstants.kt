package com.zekibiyikli.nativemindscase.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Event ve parametre isimleri tek yerde.
 *
 * Duz string kullanilmiyor cunku Firebase yanlis yazilmis bir ismi sessizce
 * kabul ediyor: event gider, hata alinmaz, sadece raporda gorunmez. Yazim
 * hatasini fark etmek neredeyse imkansiz oldugundan isimler buradan geciyor.
 *
 * Karsiligi olan yerlerde Firebase'in kendi sabitleri kullaniliyor; bu
 * event'ler GA4'te hazir raporlara dusuyor, custom isim uydurulursa her
 * raporu elle kurmak gerekir.
 */
object AnalyticsEvent {

    /** Kitap detayi acildi. */
    val SELECT_ITEM: String = FirebaseAnalytics.Event.SELECT_ITEM

    val SEARCH: String = FirebaseAnalytics.Event.SEARCH

    const val SELECT_CATEGORY = "select_category"

    /** Ucretsiz okuma hakkindan biri kullanildi. */
    const val QUOTA_CONSUMED = "quota_consumed"

    /** Gunluk hak bitti, kullanici premium'a yonlendirildi. */
    const val QUOTA_EXCEEDED = "quota_exceeded"

    const val PREMIUM_OPENED = "premium_opened"
    const val PREMIUM_PLAN_SELECTED = "premium_plan_selected"
    const val PREMIUM_PURCHASED = "premium_purchased"

    /** Kitap <-> Sesli gecisi. */
    const val READ_MODE_CHANGE = "read_mode_change"

    /** Ozetin nereden geldigi: onbellek, yeni uretim ya da hic. */
    const val SUMMARY_SHOWN = "summary_shown"

    /** Ag hata verdi ama Room'daki kayitlar gosterildi. */
    const val OFFLINE_CONTENT_SHOWN = "offline_content_shown"
}

object AnalyticsParam {

    val ITEM_ID: String = FirebaseAnalytics.Param.ITEM_ID
    val ITEM_NAME: String = FirebaseAnalytics.Param.ITEM_NAME
    val SEARCH_TERM: String = FirebaseAnalytics.Param.SEARCH_TERM

    const val SOURCE = "source"
    const val CATEGORY_ID = "category_id"
    const val REMAINING = "remaining"
    const val PLAN = "plan"
    const val MODE = "mode"
    const val ERROR_TYPE = "error_type"
}

/** Kullanicinin nereden geldigi; ayni event farkli giris noktalarindan gelebiliyor. */
object AnalyticsSource {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SEARCH_RESULTS = "search_results"
    const val FAVORITES = "favorites"
    const val PREMIUM_PAGE = "premium_page"

    /** Premium sayfasi kota duvarindan acildi, kullanici kendi girmedi. */
    const val QUOTA_GATE = "quota_gate"
}

/** [AnalyticsEvent.SUMMARY_SHOWN] icin olasi degerler. */
object SummarySource {
    const val CACHE = "cache"
    const val GENERATED = "generated"

    /** Model kitabi tanimadi ya da istek reddedildi. */
    const val NONE = "none"

    /** API anahtari tanimli degil. */
    const val DISABLED = "disabled"
}

/**
 * Kullanici ozelligi olarak set edilir, event parametresi olarak degil:
 * boylece GA4'teki her rapor premium/ucretsiz diye ayrilabiliyor.
 */
object AnalyticsUserProperty {
    const val IS_PREMIUM = "is_premium"
}
