package com.hman.podcastreader.data.repository

import com.hman.podcastreader.data.manager.AudioDownloadManager
import com.hman.podcastreader.domain.repository.AudioDownloadRepository
import javax.inject.Inject

class AudioDownloadRepositoryImpl
@Inject
constructor(private val audioDownloadManager: AudioDownloadManager) : AudioDownloadRepository {
    override suspend fun downloadAudio(audioUrl: String, articleTitle: String): Result<String> {
        return audioDownloadManager.downloadAudio(audioUrl, articleTitle).map { downloadResult ->
            downloadResult.filePath
        }
    }
}
