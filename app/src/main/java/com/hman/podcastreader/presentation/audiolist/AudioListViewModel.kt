package com.hman.podcastreader.presentation.audiolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hman.podcastreader.domain.model.DownloadedAudio
import com.hman.podcastreader.domain.usecase.DeleteDownloadedAudioUseCase
import com.hman.podcastreader.domain.usecase.GetDownloadedAudiosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioListViewModel
@Inject
constructor(
    private val getDownloadedAudiosUseCase: GetDownloadedAudiosUseCase,
    private val deleteDownloadedAudioUseCase: DeleteDownloadedAudioUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AudioListState())
    val state: StateFlow<AudioListState> = _state.asStateFlow()

    init {
        loadDownloadedAudios()
    }

    fun onEvent(event: AudioListEvent) {
        when (event) {
            is AudioListEvent.DeleteAudio -> deleteAudio(event.id)
            is AudioListEvent.PlayAudio -> playAudio(event.audio)
            is AudioListEvent.DismissPlayer -> dismissPlayer()
            is AudioListEvent.ExpandPlayer -> expandPlayer()
            is AudioListEvent.MinimizePlayer -> minimizePlayer()
            is AudioListEvent.ClosePlayer -> closePlayer()
        }
    }

    private fun loadDownloadedAudios() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getDownloadedAudiosUseCase()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load downloaded audios"
                        )
                    }
                }
                .collect { audios ->
                    _state.update {
                        it.copy(downloadedAudios = audios, isLoading = false, error = null)
                    }
                }
        }
    }

    private fun deleteAudio(id: String) {
        viewModelScope.launch {
            deleteDownloadedAudioUseCase(id).onFailure { error ->
                _state.update { it.copy(error = error.message ?: "Failed to delete audio") }
            }
        }
    }

    private fun playAudio(audio: DownloadedAudio) {
        _state.update { it.copy(currentPlayingAudio = audio, isPlayerExpanded = true) }
    }

    private fun dismissPlayer() {
        _state.update { it.copy(currentPlayingAudio = null, isPlayerExpanded = false) }
    }

    private fun expandPlayer() {
        _state.update { it.copy(isPlayerExpanded = true) }
    }

    private fun minimizePlayer() {
        _state.update { it.copy(isPlayerExpanded = false) }
    }

    private fun closePlayer() {
        _state.update { it.copy(currentPlayingAudio = null, isPlayerExpanded = false) }
    }
}
