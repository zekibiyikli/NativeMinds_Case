package com.zekibiyikli.nativemindscase.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBookDao {

    /** Periyodik sync favorileri tazelerken tam listeyi okuyor. */
    @Query("SELECT * FROM favorite_books ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteBookEntity>>

    /**
     * Favoriler ekrani icin sayfali okuma. Room PagingSource'u kendisi
     * uretiyor ve tablo degistiginde otomatik gecersiz kiliyor.
     */
    @Query("SELECT * FROM favorite_books ORDER BY savedAt DESC")
    fun pagingSource(): PagingSource<Int, FavoriteBookEntity>

    /** Kalp ikonunun dolu/bos durumu icin; tum satirlari cekmeye gerek yok. */
    @Query("SELECT id FROM favorite_books")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    /** Detay ekrani cevrimdisiyken favoriye dusebilsin diye. */
    @Query("SELECT * FROM favorite_books WHERE id = :id LIMIT 1")
    suspend fun find(id: String): FavoriteBookEntity?

    @Upsert
    suspend fun upsert(book: FavoriteBookEntity)

    @Query("DELETE FROM favorite_books WHERE id = :id")
    suspend fun deleteById(id: String)
}
