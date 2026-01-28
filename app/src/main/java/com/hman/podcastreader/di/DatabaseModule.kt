package com.hman.podcastreader.di

import android.content.Context
import androidx.room.Room
import com.hman.podcastreader.data.dataSource.local.dao.ArticleDao
import com.hman.podcastreader.data.dataSource.local.database.AppDatabase
import com.hman.podcastreader.data.local.dao.DownloadedAudioDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration() // For development - will recreate database
                .build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: AppDatabase): ArticleDao {
        return database.articleDao()
    }

    @Provides
    @Singleton
    fun provideDownloadedAudioDao(database: AppDatabase): DownloadedAudioDao {
        return database.downloadedAudioDao()
    }
}
