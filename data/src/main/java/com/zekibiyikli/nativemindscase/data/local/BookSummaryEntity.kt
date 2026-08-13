package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Uretilen ozetin onbellegi. Ayni kitap icin ikinci kez istek atilmaz.
 *
 * [summary] null olabilir: model kitabi tanimadigini soyledi ya da ozet
 * uretilemedi. Bu durum da kaydediliyor, yoksa her acilista bosuna yeniden
 * denenirdi.
 */
@Entity(tableName = "book_summaries")
data class BookSummaryEntity(
    /** Google Books volume ID. */
    @PrimaryKey val id: String,
    val summary: String?,
    val generatedAt: Long
)
