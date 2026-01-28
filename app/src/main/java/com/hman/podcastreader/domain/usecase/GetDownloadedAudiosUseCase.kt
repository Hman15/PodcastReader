package com.hman.podcastreader.domain.usecase

import com.hman.podcastreader.domain.model.DownloadedAudio
import com.hman.podcastreader.domain.repository.DownloadedAudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadedAudiosUseCase
@Inject
constructor(private val downloadedAudioRepository: DownloadedAudioRepository) {
    operator fun invoke(): Flow<List<DownloadedAudio>> {
        return downloadedAudioRepository.getAllDownloadedAudios()
    }
}
