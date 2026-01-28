package com.hman.podcastreader.data.mapper

import com.hman.podcastreader.data.dataSource.local.entity.ArticleEntity
import com.hman.podcastreader.domain.model.Article

fun ArticleEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        articleUrl = articleUrl,
        audioUrl = audioUrl,
        source = source
    )
}

fun Article.toEntity(): ArticleEntity {
    return ArticleEntity(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        articleUrl = articleUrl,
        audioUrl = audioUrl,
        source = source
    )
}