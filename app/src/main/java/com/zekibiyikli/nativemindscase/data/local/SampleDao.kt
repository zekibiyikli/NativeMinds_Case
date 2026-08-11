package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {

    /** Flow dondurur: tablo degistiginde UI otomatik guncellenir. */
    @Query("SELECT * FROM sample_items ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SampleEntity>>

    @Query("SELECT * FROM sample_items WHERE id = :id")
    suspend fun getById(id: Long): SampleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SampleEntity>)

    @Query("DELETE FROM sample_items")
    suspend fun clear()
}
