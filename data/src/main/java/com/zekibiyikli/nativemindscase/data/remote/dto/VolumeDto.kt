package com.zekibiyikli.nativemindscase.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * /volumes yaniti. Sonuc yoksa Google "items" alanini hic gondermiyor,
 * bu yuzden nullable.
 */
@Serializable
data class VolumesResponseDto(
    @SerialName("totalItems") val totalItems: Int = 0,
    @SerialName("items") val items: List<VolumeDto>? = null
)

@Serializable
data class VolumeDto(
    @SerialName("id") val id: String,
    @SerialName("volumeInfo") val volumeInfo: VolumeInfoDto? = null,
    @SerialName("accessInfo") val accessInfo: AccessInfoDto? = null
)

@Serializable
data class VolumeInfoDto(
    @SerialName("title") val title: String? = null,
    @SerialName("subtitle") val subtitle: String? = null,
    @SerialName("authors") val authors: List<String>? = null,
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("publishedDate") val publishedDate: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("pageCount") val pageCount: Int? = null,
    @SerialName("categories") val categories: List<String>? = null,
    @SerialName("averageRating") val averageRating: Double? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("previewLink") val previewLink: String? = null,
    @SerialName("imageLinks") val imageLinks: ImageLinksDto? = null
)

@Serializable
data class ImageLinksDto(
    @SerialName("smallThumbnail") val smallThumbnail: String? = null,
    @SerialName("thumbnail") val thumbnail: String? = null
)

@Serializable
data class AccessInfoDto(
    /** FULL | PARTIAL | NO_PAGES — icerigin okunabilirligi. */
    @SerialName("viewability") val viewability: String? = null,
    @SerialName("webReaderLink") val webReaderLink: String? = null
)
