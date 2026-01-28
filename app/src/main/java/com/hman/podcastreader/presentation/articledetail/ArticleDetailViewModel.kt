package com.hman.podcastreader.presentation.articledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hman.podcastreader.domain.repository.ArticleRepository
import com.hman.podcastreader.domain.usecase.DownloadAudioUseCase
import com.hman.podcastreader.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel
@Inject
constructor(
    private val articleRepository: ArticleRepository,
    private val downloadAudioUseCase: DownloadAudioUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleDetailState())
    val state: StateFlow<ArticleDetailState> = _state.asStateFlow()

    private var articleContent: String = ""

    fun onEvent(event: ArticleDetailEvent) {
        when (event) {
            is ArticleDetailEvent.LoadArticle -> loadArticle(event.articleId)
            is ArticleDetailEvent.AudioUrlsDetected -> onAudioUrlsDetected(event.urls)
            is ArticleDetailEvent.ContentExtracted -> onContentExtracted(event.content)
            is ArticleDetailEvent.DownloadAudio -> downloadAudio(event.audioUrl)
        }
    }

    private fun loadArticle(articleId: String) {
        viewModelScope.launch {
            _state.update { it.copy(article = UiState.Loading) }

            articleRepository
                .getArticleById(articleId)
                .fold(
                    onSuccess = { article ->
                        if (article != null) {
                            _state.update { it.copy(article = UiState.Success(article)) }
                        } else {
                            _state.update {
                                it.copy(article = UiState.Error("Article not found"))
                            }
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                article =
                                    UiState.Error(
                                        message = error.message
                                            ?: "Failed to load article"
                                    )
                            )
                        }
                    }
                )
        }
    }

    private fun onAudioUrlsDetected(urls: List<String>) {
        _state.update { currentState ->
            val newUrls = (currentState.audioUrls + urls).distinct()
            currentState.copy(audioUrls = newUrls)
        }
    }

    private fun onContentExtracted(content: String) {
        articleContent = content
    }

    private fun downloadAudio(audioUrl: String) {
        val currentArticle = (_state.value.article as? UiState.Success)?.data ?: return

        viewModelScope.launch {
            downloadAudioUseCase(audioUrl, currentArticle.title)
                .onSuccess { _state.update { it.copy(downloadError = null) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(downloadError = error.message ?: "Download failed")
                    }
                }
        }
    }
}
