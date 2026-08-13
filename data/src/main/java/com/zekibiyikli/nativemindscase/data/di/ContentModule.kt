package com.zekibiyikli.nativemindscase.data.di

import com.zekibiyikli.nativemindscase.data.content.ContentRepository
import com.zekibiyikli.nativemindscase.data.content.GoogleBooksContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentModule {

    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: GoogleBooksContentRepository): ContentRepository
}
