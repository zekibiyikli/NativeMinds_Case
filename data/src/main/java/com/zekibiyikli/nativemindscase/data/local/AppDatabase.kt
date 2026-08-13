package com.zekibiyikli.nativemindscase.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FavoriteBookEntity::class,
        BookSummaryEntity::class,
        CachedBookEntity::class,
        FeedRemoteKeyEntity::class
    ],
    // v1: sample_items (kaldirildi), v2: favorite_books, v3: + book_summaries,
    // v4: + cached_books & feed_remote_keys (feed artik yerelden okunuyor)
    version = 4,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteBookDao(): FavoriteBookDao

    abstract fun bookSummaryDao(): BookSummaryDao

    abstract fun cachedBookDao(): CachedBookDao

    abstract fun feedRemoteKeyDao(): FeedRemoteKeyDao
}
