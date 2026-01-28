package com.hman.podcastreader.presentation.articlelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hman.podcastreader.domain.model.Article
import com.hman.podcastreader.domain.usecase.GetArticlesUseCase
import com.hman.podcastreader.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Article>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Article>>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var currentArticles: List<Article> = emptyList()

    init {
        loadArticles()
    }

    fun loadArticles() {
        viewModelScope.launch {
            val isRefresh = _isRefreshing.value

            if (!isRefresh) {
                _uiState.value = UiState.Loading
            }

            getArticlesUseCase().fold(
                onSuccess = { articles ->
                    currentArticles = articles
                    _uiState.value =
                        if (articles.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(articles)
                        }
                    _isRefreshing.value = false
                },
                onFailure = { error ->
                    val errorMessage = getErrorMessage(error)

                    if (isRefresh && currentArticles.isNotEmpty()) {
                        _uiState.value = UiState.Success(currentArticles)
                        _toastMessage.value = errorMessage
                    } else {
                        _uiState.value =
                            UiState.Error(message = errorMessage, throwable = error)
                    }
                    _isRefreshing.value = false
                }
            )
        }
    }

    private fun getErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                "No Internet Connection"

            error.message?.contains("timeout", ignoreCase = true) == true ->
                "No Internet Connection"

            error.message?.contains("network", ignoreCase = true) == true ->
                "No Internet Connection"

            error is java.net.UnknownHostException -> "No Internet Connection"
            error is java.net.SocketTimeoutException -> "No Internet Connection"
            else -> error.message ?: "Failed to load articles"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun refresh() {
        _isRefreshing.value = true
        loadArticles()
    }

    fun retry() {
        loadArticles()
    }
}
