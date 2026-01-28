package com.hman.podcastreader.data.local.dao

import androidx.room.*
import com.hman.podcastreader.data.local.entity.DownloadedAudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedAudioDao {
    @Query("SELECT * FROM downloaded_audios ORDER BY downloadedAt DESC")
    fun getAllDownloadedAudios(): Flow<List<DownloadedAudioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedAudio(audio: DownloadedAudioEntity)

    @Query("DELETE FROM downloaded_audios WHERE id = :id")
    suspend fun deleteDownloadedAudio(id: String)

    @Query("SELECT * FROM downloaded_audios WHERE id = :id")
    suspend fun getDownloadedAudioById(id: String): DownloadedAudioEntity?
}
