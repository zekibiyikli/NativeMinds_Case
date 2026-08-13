package com.zekibiyikli.nativemindscase.data.remote

import com.zekibiyikli.nativemindscase.data.content.model.ContentItem
import com.zekibiyikli.nativemindscase.data.remote.dto.VolumeDto

/** Google Books description alani HTML parcaciklari icerebiliyor. */
private val HTML_TAG = Regex("<[^>]*>")

fun VolumeDto.toContentItem(): ContentItem {
    val info = volumeInfo

    return ContentItem(
        id = id,
        title = listOfNotNull(
            info?.title?.takeIf { it.isNotBlank() },
            info?.subtitle?.takeIf { it.isNotBlank() }
        ).joinToString(separator = ": "),
        author = info?.authors.orEmpty().joinToString(separator = ", "),
        coverUrl = (info?.imageLinks?.thumbnail ?: info?.imageLinks?.smallThumbnail)?.toHttps(),
        description = info?.description.orEmpty().stripHtml(),
        pageCount = info?.pageCount ?: 0,
        publishedDate = info?.publishedDate,
        categories = info?.categories.orEmpty()
    )
}

/**
 * Google kapak linklerini http olarak donuyor; Android varsayilan olarak
 * cleartext trafigi engelledigi icin gorseller yuklenmez.
 */
private fun String.toHttps(): String =
    if (startsWith("http://")) "https://" + removePrefix("http://") else this

private fun String.stripHtml(): String = replace(HTML_TAG, "")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&amp;", "&")
    .replace("&nbsp;", " ")
    .lineSequence()
    .joinToString("\n") { it.trim() }
    .trim()
