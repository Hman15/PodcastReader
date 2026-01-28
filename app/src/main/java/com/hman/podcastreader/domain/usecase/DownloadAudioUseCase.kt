package com.hman.podcastreader.domain.usecase

import com.hman.podcastreader.domain.model.DownloadedAudio
import com.hman.podcastreader.domain.repository.AudioDownloadRepository
import com.hman.podcastreader.domain.repository.DownloadedAudioRepository
import java.util.UUID
import javax.inject.Inject

class DownloadAudioUseCase
@Inject
constructor(
    private val audioDownloadRepository: AudioDownloadRepository,
    private val downloadedAudioRepository: DownloadedAudioRepository
) {
    suspend operator fun invoke(audioUrl: String, articleTitle: String): Result<Unit> {
        // Start the download and get the file path
        return audioDownloadRepository
            .downloadAudio(audioUrl, articleTitle)
            .onSuccess { filePath ->
                // Save the downloaded audio record to database
                val downloadedAudio =
                    DownloadedAudio(
                        id = UUID.randomUUID().toString(),
                        articleTitle = articleTitle,
                        audioFilePath = filePath,
                        downloadedAt = System.currentTimeMillis()
                    )
                downloadedAudioRepository.saveDownloadedAudio(downloadedAudio)
            }
            .map {} // Convert Result<String> to Result<Unit>
    }
}
