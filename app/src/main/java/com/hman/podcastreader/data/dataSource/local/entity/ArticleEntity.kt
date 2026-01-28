package com.hman.podcastreader.data.dataSource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val articleUrl: String,
    val audioUrl: String?,
    val source: String
)