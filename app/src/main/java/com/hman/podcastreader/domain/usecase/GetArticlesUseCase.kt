package com.hman.podcastreader.domain.usecase

import com.hman.podcastreader.domain.model.Article
import com.hman.podcastreader.domain.repository.ArticleRepository
import javax.inject.Inject

class GetArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(): Result<List<Article>> {
        return repository.fetchArticles()
    }
}
