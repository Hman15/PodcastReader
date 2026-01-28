package com.hman.podcastreader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_audios")
data class DownloadedAudioEntity(
        @PrimaryKey val id: String,
        val articleTitle: String,
        val audioFilePath: String,
        val downloadedAt: Long
)
