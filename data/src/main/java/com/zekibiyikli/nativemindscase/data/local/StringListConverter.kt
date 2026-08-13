package com.zekibiyikli.nativemindscase.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Kategori listesini tek sutuna yazar.
 *
 * Ayirici karakterle birlestirmek yerine JSON: Google Books kategorileri
 * virgul ve egik cizgi icerebiliyor ("Fiction / Science Fiction, General"),
 * duz birlestirme bu degerlerde bozulur.
 */
class StringListConverter {

    @TypeConverter
    fun fromList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> =
        // Bozuk bir kayit yuzunden sorgu patlamasin; kategori kritik alan degil.
        runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())

    private companion object {
        val json = Json
    }
}
