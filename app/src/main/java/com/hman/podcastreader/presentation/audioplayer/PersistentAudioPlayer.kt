package com.hman.podcastreader.presentation.audioplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PersistentAudioPlayer(
    isVisible: Boolean,
    audioUrl: String,
    title: String,
    onCollapse: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box {
            AudioPlayerView(
                audioUrl = audioUrl,
                title = title,
                onCollapse = onCollapse,
                onClose = onClose
            )
        }
    }
}
