package com.hman.podcastreader.presentation.audioplayer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton object to manage global audio player state across the entire app. This ensures the
 * player state is shared between all screens and ViewModels.
 */
object GlobalAudioPlayerState {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun setCurrentAudio(audioFilePath: String, title: String) {
        _playerState.value =
                _playerState.value.copy(
                        currentAudioUrl = audioFilePath,
                        currentTitle = title,
                        isVisible = true,
                        isExpanded = false
                )
    }

    fun expandPlayer() {
        _playerState.value = _playerState.value.copy(isExpanded = true)
    }

    fun minimizePlayer() {
        _playerState.value = _playerState.value.copy(isExpanded = false)
    }

    fun closePlayer() {
        _playerState.value = PlayerState()
    }

    fun isPlayerVisible(): Boolean = _playerState.value.isVisible
}

data class PlayerState(
        val currentAudioUrl: String = "",
        val currentTitle: String = "",
        val isVisible: Boolean = false,
        val isExpanded: Boolean = false
)
