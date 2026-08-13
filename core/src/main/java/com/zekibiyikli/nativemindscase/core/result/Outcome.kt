package com.zekibiyikli.nativemindscase.core.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Ag destekli akislarin uc durumu. kotlin.Result ile karismasin diye
 * ayri isim; ayrica Loading durumunu tasiyabiliyor.
 */
sealed interface Outcome<out T> {

    data object Loading : Outcome<Nothing>

    data class Success<T>(val data: T) : Outcome<T>

    data class Failure(val throwable: Throwable) : Outcome<Nothing>
}

/** Basari degerini, yoksa null. */
fun <T> Outcome<T>.dataOrNull(): T? = (this as? Outcome.Success)?.data

/**
 * Bir veri akisini Outcome'a sarar: basta Loading yayar, hatayi
 * akisi oldurmeden Failure'a cevirir.
 */
fun <T> Flow<T>.asOutcome(): Flow<Outcome<T>> = this
    .map<T, Outcome<T>> { Outcome.Success(it) }
    .onStart { emit(Outcome.Loading) }
    .catch { emit(Outcome.Failure(it)) }
