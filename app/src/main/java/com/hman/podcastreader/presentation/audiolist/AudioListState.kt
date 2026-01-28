package com.hman.podcastreader.presentation.audiolist

import com.hman.podcastreader.domain.model.DownloadedAudio

data class AudioListState(
    val downloadedAudios: List<DownloadedAudio> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPlayingAudio: DownloadedAudio? = null,
    val isPlayerExpanded: Boolean = false
)
