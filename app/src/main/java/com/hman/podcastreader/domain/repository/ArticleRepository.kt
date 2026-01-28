package com.hman.podcastreader.domain.repository

import com.hman.podcastreader.domain.model.Article

interface ArticleRepository {
    /** Force refresh articles from remote sources */
    suspend fun fetchArticles(): Result<List<Article>>

    /** Get a specific article by ID */
    suspend fun getArticleById(id: String): Result<Article?>
}
