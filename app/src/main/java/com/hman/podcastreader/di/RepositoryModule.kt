package com.hman.podcastreader.di

import com.hman.podcastreader.data.repository.ArticleRepositoryImpl
import com.hman.podcastreader.data.repository.DownloadedAudioRepositoryImpl
import com.hman.podcastreader.domain.repository.ArticleRepository
import com.hman.podcastreader.domain.repository.DownloadedAudioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindAudioDownloadRepository(
            impl: com.hman.podcastreader.data.repository.AudioDownloadRepositoryImpl
    ): com.hman.podcastreader.domain.repository.AudioDownloadRepository

    @Binds
    @Singleton
    abstract fun bindDownloadedAudioRepository(
            impl: DownloadedAudioRepositoryImpl
    ): DownloadedAudioRepository
}
