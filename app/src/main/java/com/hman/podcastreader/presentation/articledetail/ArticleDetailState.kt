package com.hman.podcastreader.presentation.articledetail

import com.hman.podcastreader.domain.model.Article
import com.hman.podcastreader.presentation.common.UiState

data class ArticleDetailState(
        val article: UiState<Article> = UiState.Loading,
        val audioUrls: List<String> = emptyList(),
        val downloadError: String? = null
)
