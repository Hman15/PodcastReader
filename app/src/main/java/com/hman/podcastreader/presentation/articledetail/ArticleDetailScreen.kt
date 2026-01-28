package com.hman.podcastreader.presentation.articledetail

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hman.podcastreader.R
import com.hman.podcastreader.domain.model.Article
import com.hman.podcastreader.presentation.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    onNavigateBack: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val webViewBridge = remember { WebViewBridge() }

    LaunchedEffect(articleId) { viewModel.onEvent(ArticleDetailEvent.LoadArticle(articleId)) }

    // Collect audio URLs from bridge
    LaunchedEffect(Unit) {
        webViewBridge.audioUrls.collect { urls ->
            viewModel.onEvent(ArticleDetailEvent.AudioUrlsDetected(urls))
        }
    }

    // Collect content from bridge
    LaunchedEffect(Unit) {
        webViewBridge.content.collect { content ->
            viewModel.onEvent(ArticleDetailEvent.ContentExtracted(content))
        }
    }

    // Show download error toast
    LaunchedEffect(state.downloadError) {
        state.downloadError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    ArticleDetailContent(
        state = state,
        webViewBridge = webViewBridge,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailContent(
    state: ArticleDetailState,
    webViewBridge: WebViewBridge,
    onEvent: (ArticleDetailEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Download button (shown when audio URLs detected)
                    if (state.audioUrls.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onEvent(
                                    ArticleDetailEvent.DownloadAudio(
                                        state.audioUrls.first()
                                    )
                                )
                                Toast.makeText(
                                    context,
                                    "Download started",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = "Download"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val articleState = state.article) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is UiState.Success -> {
                ArticleWebView(
                    article = articleState.data,
                    webViewBridge = webViewBridge,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = articleState.message, color = MaterialTheme.colorScheme.error)
                        Button(
                            onClick = {
                                (state.article as? UiState.Success)?.data?.let { article ->
                                    onEvent(ArticleDetailEvent.LoadArticle(article.articleUrl))
                                }
                            }
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_refresh), null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }

            is UiState.Empty -> {}
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleWebView(article: Article, webViewBridge: WebViewBridge, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }

                addJavascriptInterface(webViewBridge, "Android")

                webViewClient =
                    object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)

                            // Inject JavaScript to extract audio URLs
                            // Supports both Dantri and VNExpress
                            val audioExtractionScript = AUDIO_DETECT_SCRIPT.trimIndent()

                            view?.evaluateJavascript(audioExtractionScript, null)
                        }
                    }

                loadUrl(article.articleUrl)
            }
        }
    )
}

private const val AUDIO_DETECT_SCRIPT = """
                        (function() {
                            if (window.__audioObserverInstalled) return;
                            window.__audioObserverInstalled = true;

                            const currentUrl = window.location.href;
                            const isDantri = currentUrl.includes('dantri.com');
                            const isVnExpress = currentUrl.includes('vnexpress.net');

                            console.log('Installing audio observer for:', isDantri ? 'Dantri' : isVnExpress ? 'VNExpress' : 'Unknown');

                            function tryExtractDantri() {
                                const audio = document.querySelector('audio[src]');
                                if (audio && audio.src) {
                                    console.log('Dantri audio detected:', audio.src);
                                    Android.onAudioDetected(JSON.stringify([audio.src]));
                                    return true;
                                }
                                return false;
                            }

                            function tryExtractVnExpress() {
                                const audio = document.querySelector('audio#audio-5010625[src], audio[src][type="audio/mpeg"]');
                                if (audio && audio.src) {
                                    console.log('VNExpress audio detected:', audio.src);
                                    Android.onAudioDetected(JSON.stringify([audio.src]));
                                    return true;
                                }
                                return false;
                            }

                            function tryExtract() {
                                let found = false;
                                if (isDantri) {
                                    found = tryExtractDantri();
                                } else if (isVnExpress) {
                                    found = tryExtractVnExpress();
                                } else {
                                    // Try both if site is unknown
                                    found = tryExtractDantri() || tryExtractVnExpress();
                                }
                                
                                if (found) {
                                    observer.disconnect();
                                }
                            }

                            const observer = new MutationObserver(() => {
                                tryExtract();
                            });

                            observer.observe(document.documentElement, {
                                childList: true,    
                                subtree: true
                            });

                            // Try immediately too
                            tryExtract();
                        })();
                        """