package com.zekibiyikli.nativemindscase.data.content.model

/**
 * Feed'deki kategori cipleri.
 *
 * Google Books'ta kategorileri listeleyen bir endpoint yok; [id] dogrudan
 * "subject:" filtresine giden deger, [name] ise ekranda gosterilen etiket.
 */
data class Subject(
    val id: String,
    val name: String
)

/** Bir Google Books volume'unun uygulamada kullanilan hali. */
data class ContentItem(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String,
    val pageCount: Int,
    val publishedDate: String?,
    val categories: List<String>
)
