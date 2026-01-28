package com.hman.podcastreader.data.dataSource.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hman.podcastreader.data.dataSource.local.dao.ArticleDao
import com.hman.podcastreader.data.dataSource.local.entity.ArticleEntity
import com.hman.podcastreader.data.local.dao.DownloadedAudioDao
import com.hman.podcastreader.data.local.entity.DownloadedAudioEntity

@Database(
        entities = [ArticleEntity::class, DownloadedAudioEntity::class],
        version = 4,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun downloadedAudioDao(): DownloadedAudioDao

    companion object {
        const val DATABASE_NAME = "podcast_reader_db"
    }
}
