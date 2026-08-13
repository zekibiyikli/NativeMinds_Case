package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface BookSummaryDao {

    @Query("SELECT * FROM book_summaries WHERE id = :id")
    suspend fun find(id: String): BookSummaryEntity?

    @Upsert
    suspend fun upsert(summary: BookSummaryEntity)

    /** Ozetleri elle tazelemek gerekirse. */
    @Query("DELETE FROM book_summaries WHERE id = :id")
    suspend fun deleteById(id: String)
}
