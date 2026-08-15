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
