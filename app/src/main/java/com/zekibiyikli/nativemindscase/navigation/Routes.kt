package com.zekibiyikli.nativemindscase.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route'lari. Argumanlar string yerine
 * bu siniflarin alanlari uzerinden tasinir.
 */
@Serializable
data object SplashRoute

@Serializable
data object HomeRoute

@Serializable
data object SearchRoute

/**
 * Ikisi birden verilirse Google Books'un "kelime + subject:" birlesik
 * filtresi kullanilir.
 */
@Serializable
data class SearchResultsRoute(
    val query: String = "",
    val subjectId: String? = null
)

@Serializable
data object FavoritesRoute

/**
 * [source] yalnizca analitik icin: ayni ekrana dort ayri yerden geliniyor,
 * hangi giris noktasinin calistigi baska turlu olculemiyor.
 */
@Serializable
data class DetailRoute(
    val itemId: String,
    val source: String
)

/** [source]: kota duvarindan mi geldi yoksa kullanici kendi mi girdi. */
@Serializable
data class PremiumRoute(val source: String)
