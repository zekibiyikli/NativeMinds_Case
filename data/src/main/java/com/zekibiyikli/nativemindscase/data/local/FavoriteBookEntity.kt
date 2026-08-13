package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Favoriler cevrimdisi de listelenebilmeli, bu yuzden sadece ID degil
 * karti cizmeye yetecek alanlar da saklaniyor.
 */
@Entity(tableName = "favorite_books")
data class FavoriteBookEntity(
    /** Google Books volume ID. */
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String,
    val pageCount: Int,
    val publishedDate: String?,
    val savedAt: Long
)
