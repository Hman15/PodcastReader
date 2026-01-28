package com.hman.podcastreader.domain.repository

interface AudioDownloadRepository {
    /**
     * Download audio file from the given URL
     * @param audioUrl The URL of the audio file to download
     * @param articleTitle The title of the article for naming the file
     * @return Result with file path on success, or error on failure
     */
    suspend fun downloadAudio(audioUrl: String, articleTitle: String): Result<String>
}
