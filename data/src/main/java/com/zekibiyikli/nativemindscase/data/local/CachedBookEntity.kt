package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Entity
import com.zekibiyikli.nativemindscase.data.content.model.ContentItem

/**
 * Feed'in yerel kopyasi — ekranin okudugu tek kaynak.
 *
 * Ag'dan gelen sayfalar buraya yazilir, liste her zaman buradan okunur.
 * Boylece internet yokken de son gorulen icerik ekranda kalir.
 *
 * Birincil anahtar (subjectId, id) ciftidir: ayni kitap birden fazla
 * kategoride gorunebiliyor ve her kategorideki sirasi farkli.
 */
@Entity(tableName = "cached_books", primaryKeys = ["subjectId", "id"])
data class CachedBookEntity(
    /** Hangi kategori feed'ine ait. */
    val subjectId: String,

    /** Google Books volume ID. */
    val id: String,

    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String,
    val pageCount: Int,
    val publishedDate: String?,
    val categories: List<String>,

    /**
     * API'den gelis sirasi. Room'un sorgusu bununla siraliyor; yoksa
     * sayfalar arasi sira kaybolur ve liste her acilista karisir.
     */
    val position: Int
)

internal fun ContentItem.toCachedEntity(subjectId: String, position: Int) = CachedBookEntity(
    subjectId = subjectId,
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    pageCount = pageCount,
    publishedDate = publishedDate,
    categories = categories,
    position = position
)

internal fun CachedBookEntity.toContentItem() = ContentItem(
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    pageCount = pageCount,
    publishedDate = publishedDate,
    categories = categories
)
