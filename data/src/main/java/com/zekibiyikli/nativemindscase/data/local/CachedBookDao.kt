package com.zekibiyikli.nativemindscase.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedBookDao {

    /**
     * Feed'in okundugu yer. Room PagingSource'u tablo degistiginde kendisi
     * gecersiz kiliyor, dolayisiyla ag'dan yeni sayfa yazilir yazilmaz
     * liste guncelleniyor.
     */
    @Query("SELECT * FROM cached_books WHERE subjectId = :subjectId ORDER BY position ASC")
    fun pagingSource(subjectId: String): PagingSource<Int, CachedBookEntity>

    /**
     * IGNORE bilerek: Google Books ayni volume'u farkli startIndex
     * degerlerinde tekrar dondurebiliyor. Upsert olsaydi kaydin position'i
     * guncellenir ve kitap listede yerinden oynardi.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(books: List<CachedBookEntity>)

    /** Sonraki sayfanin nereden numaralanacagi. */
    @Query("SELECT MAX(position) FROM cached_books WHERE subjectId = :subjectId")
    suspend fun maxPosition(subjectId: String): Int?

    @Query("DELETE FROM cached_books WHERE subjectId = :subjectId")
    suspend fun deleteBySubject(subjectId: String)

    /** Detay ekrani cevrimdisiyken yerel kopyaya dusebilsin diye. */
    @Query("SELECT * FROM cached_books WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CachedBookEntity?
}
