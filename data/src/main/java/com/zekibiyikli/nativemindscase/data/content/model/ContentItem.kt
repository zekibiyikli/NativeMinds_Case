package com.zekibiyikli.nativemindscase.data.content.model

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
