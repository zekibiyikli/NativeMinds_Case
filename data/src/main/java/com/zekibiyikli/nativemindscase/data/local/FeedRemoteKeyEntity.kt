package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Her kategori feed'i icin sayfalama durumu.
 *
 * Ag'dan gelen sayfa anahtari (startIndex) bellekte degil burada tutuluyor;
 * uygulama kapanip acildiginda kaldigi yerden devam edebilsin diye.
 */
@Entity(tableName = "feed_remote_keys")
data class FeedRemoteKeyEntity(
    @PrimaryKey val subjectId: String,

    /** Bir sonraki istekte kullanilacak startIndex. */
    val nextStartIndex: Int,

    /** API bu kategoride veri kalmadigini bildirdi. */
    val endReached: Boolean,

    /** Onbellegin bayatlayip bayatlamadigina bakmak icin. */
    val refreshedAt: Long
)
