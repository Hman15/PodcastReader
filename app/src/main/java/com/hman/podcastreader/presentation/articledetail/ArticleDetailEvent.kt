package com.hman.podcastreader.presentation.articledetail

sealed interface ArticleDetailEvent {
    data class LoadArticle(val articleId: String) : ArticleDetailEvent
    data class AudioUrlsDetected(val urls: List<String>) : ArticleDetailEvent
    data class ContentExtracted(val content: String) : ArticleDetailEvent
    data class DownloadAudio(val audioUrl: String) : ArticleDetailEvent
}
