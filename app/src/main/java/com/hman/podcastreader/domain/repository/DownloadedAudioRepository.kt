package com.hman.podcastreader.domain.repository

import com.hman.podcastreader.domain.model.DownloadedAudio
import kotlinx.coroutines.flow.Flow

interface DownloadedAudioRepository {
    /**
     * Get all downloaded audios
     * @return Flow of list of downloaded audios
     */
    fun getAllDownloadedAudios(): Flow<List<DownloadedAudio>>

    /**
     * Save a downloaded audio record
     * @param downloadedAudio The downloaded audio to save
     * @return Result indicating success or failure
     */
    suspend fun saveDownloadedAudio(downloadedAudio: DownloadedAudio): Result<Unit>

    /**
     * Delete a downloaded audio record and file
     * @param id The ID of the downloaded audio to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteDownloadedAudio(id: String): Result<Unit>

    /**
     * Clean up orphaned records where the audio file no longer exists
     * @return Number of orphaned records cleaned up
     */
    suspend fun cleanupOrphanedRecords(): Int
}
