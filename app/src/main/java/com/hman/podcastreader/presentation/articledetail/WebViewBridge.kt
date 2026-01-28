package com.hman.podcastreader.presentation.articledetail

import android.webkit.JavascriptInterface
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class WebViewBridge {
    private val _audioUrlChannel = Channel<List<String>>(Channel.BUFFERED)
    val audioUrls = _audioUrlChannel.receiveAsFlow()

    private val _contentChannel = Channel<String>(Channel.BUFFERED)
    val content = _contentChannel.receiveAsFlow()

    @JavascriptInterface
    fun onAudioDetected(audioUrlsJson: String) {
        try {
            // Parse JSON array of audio URLs
            val urls = audioUrlsJson
                .removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }

            if (urls.isNotEmpty()) {
                _audioUrlChannel.trySend(urls)
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
    }

    @JavascriptInterface
    fun onContentExtracted(htmlContent: String) {
        _contentChannel.trySend(htmlContent)
    }
}
