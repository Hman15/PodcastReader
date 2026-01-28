package com.hman.podcastreader.presentation.audioplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioPlayerViewModel @Inject constructor() : ViewModel() {
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private var audioPlayerService: AudioPlayerService? = null
    
    fun bindService(service: AudioPlayerService) {
        audioPlayerService = service
        
        // Observe service playback state
        viewModelScope.launch {
            service.playbackState.collect { state ->
                _playbackState.value = state
            }
        }
    }
    
    fun playAudio(audioUrl: String, title: String) {
        audioPlayerService?.playAudio(audioUrl, title)
    }
    
    fun togglePlayPause() {
        audioPlayerService?.togglePlayPause()
    }
    
    fun seekTo(positionMs: Long) {
        audioPlayerService?.seekTo(positionMs)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        audioPlayerService?.setPlaybackSpeed(speed)
    }
    
    fun skipForward() {
        audioPlayerService?.skipForward(15)
    }
    
    fun skipBackward() {
        audioPlayerService?.skipBackward(15)
    }
}
