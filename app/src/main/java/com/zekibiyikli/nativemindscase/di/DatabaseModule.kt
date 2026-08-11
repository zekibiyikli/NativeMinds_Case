package com.zekibiyikli.nativemindscase.di

import android.content.Context
import androidx.room.Room
import com.zekibiyikli.nativemindscase.data.local.AppDatabase
import com.zekibiyikli.nativemindscase.data.local.SampleDao
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
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            // Sema stabilize olana kadar; production oncesi gercek migration'lar yazilacak.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideSampleDao(database: AppDatabase): SampleDao = database.sampleDao()
}
