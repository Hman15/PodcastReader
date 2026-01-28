package com.hman.podcastreader.domain.model

data class Article(
    val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val articleUrl: String,
    val audioUrl: String? = null,
    val source: String
)
