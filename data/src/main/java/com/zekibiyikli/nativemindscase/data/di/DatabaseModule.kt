package com.zekibiyikli.nativemindscase.data.di

import android.content.Context
import androidx.room.Room
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.data.local.AppDatabase
import com.zekibiyikli.nativemindscase.data.local.BookSummaryDao
import com.zekibiyikli.nativemindscase.data.local.CachedBookDao
import com.zekibiyikli.nativemindscase.data.local.FavoriteBookDao
import com.zekibiyikli.nativemindscase.data.local.FeedRemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppConfig.Storage.DATABASE_NAME)
            // Sema stabilize olana kadar; production oncesi gercek migration'lar yazilacak.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideFavoriteBookDao(database: AppDatabase): FavoriteBookDao = database.favoriteBookDao()

    @Provides
    fun provideBookSummaryDao(database: AppDatabase): BookSummaryDao = database.bookSummaryDao()

    @Provides
    fun provideCachedBookDao(database: AppDatabase): CachedBookDao = database.cachedBookDao()

    @Provides
    fun provideFeedRemoteKeyDao(database: AppDatabase): FeedRemoteKeyDao =
        database.feedRemoteKeyDao()
}
