package com.zekibiyikli.nativemindscase.data.di

import com.zekibiyikli.nativemindscase.data.summary.BookSummaryRepository
import com.zekibiyikli.nativemindscase.data.summary.ClaudeBookSummaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SummaryModule {

    @Binds
    @Singleton
    abstract fun bindBookSummaryRepository(
        impl: ClaudeBookSummaryRepository
    ): BookSummaryRepository
}
