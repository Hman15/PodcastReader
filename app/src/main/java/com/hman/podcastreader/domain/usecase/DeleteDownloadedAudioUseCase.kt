package com.hman.podcastreader.domain.usecase

import com.hman.podcastreader.domain.repository.DownloadedAudioRepository
import javax.inject.Inject

class DeleteDownloadedAudioUseCase
@Inject
constructor(private val downloadedAudioRepository: DownloadedAudioRepository) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return downloadedAudioRepository.deleteDownloadedAudio(id)
    }
}
