package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface FeedRemoteKeyDao {

    @Query("SELECT * FROM feed_remote_keys WHERE subjectId = :subjectId")
    suspend fun find(subjectId: String): FeedRemoteKeyEntity?

    @Upsert
    suspend fun upsert(key: FeedRemoteKeyEntity)
}
