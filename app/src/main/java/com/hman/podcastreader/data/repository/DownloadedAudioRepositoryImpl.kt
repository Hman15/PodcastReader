package com.hman.podcastreader.data.repository

import com.hman.podcastreader.data.dataSource.local.dao.DownloadedAudioDao
import com.hman.podcastreader.data.mapper.toDomain
import com.hman.podcastreader.data.mapper.toEntity
import com.hman.podcastreader.domain.model.DownloadedAudio
import com.hman.podcastreader.domain.repository.DownloadedAudioRepository
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DownloadedAudioRepositoryImpl
@Inject
constructor(
        private val downloadedAudioDao: DownloadedAudioDao,
) : DownloadedAudioRepository {

    override fun getAllDownloadedAudios(): Flow<List<DownloadedAudio>> {
        return downloadedAudioDao.getAllDownloadedAudios().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveDownloadedAudio(downloadedAudio: DownloadedAudio): Result<Unit> {
        return try {
            downloadedAudioDao.insertDownloadedAudio(downloadedAudio.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDownloadedAudio(id: String): Result<Unit> {
        return try {
            // Get the audio record to find the file path
            val audio = downloadedAudioDao.getDownloadedAudioById(id)

            // Delete the file if it exists
            audio?.audioFilePath?.let { filePath ->
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            }

            // Delete the database record
            downloadedAudioDao.deleteDownloadedAudio(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cleanupOrphanedRecords(): Int {
        return try {
            val allAudios = downloadedAudioDao.getAllDownloadedAudios()
            var cleanedCount = 0
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000 // 24 hours

            allAudios.collect { entities ->
                entities.forEach { entity ->
                    val file = File(entity.audioFilePath)
                    val timeSinceDownload = currentTime - entity.downloadedAt

                    // Only delete if file doesn't exist AND it's been more than 24 hours
                    // This prevents deleting records for downloads that are still in progress
                    if (!file.exists() && timeSinceDownload > oneDayInMillis) {
                        downloadedAudioDao.deleteDownloadedAudio(entity.id)
                        cleanedCount++
                    }
                }
            }

            cleanedCount
        } catch (e: Exception) {
            0
        }
    }
}
