package com.hman.podcastreader.presentation.audiolist

import com.hman.podcastreader.domain.model.DownloadedAudio

sealed interface AudioListEvent {
    data class DeleteAudio(val id: String) : AudioListEvent
    data class PlayAudio(val audio: DownloadedAudio) : AudioListEvent
    data object DismissPlayer : AudioListEvent
    data object ExpandPlayer : AudioListEvent
    data object MinimizePlayer : AudioListEvent
    data object ClosePlayer : AudioListEvent
}
