package com.hman.podcastreader.data.manager

import android.app.DownloadManager
import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadResult(val downloadId: Long, val filePath: String)

@Singleton
class AudioDownloadManager @Inject constructor(@ApplicationContext private val context: Context) {
    /**
     * Download audio file from the given URL
     * @param audioUrl The URL of the audio file to download
     * @param articleTitle The title of the article for naming the file
     * @return Result with download ID and file path on success, or error on failure
     */
    fun downloadAudio(audioUrl: String, articleTitle: String): Result<DownloadResult> {
        return try {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            // Create a safe filename from article title
            val safeFilename =
                articleTitle
                    .replace(Regex("[^a-zA-Z0-9\\s]"), "") // Remove special characters
                    .replace(Regex("\\s+"), "_") // Replace spaces with underscores
                    .take(50) // Limit length
                    .let {
                        it.ifBlank { "podcast_${System.currentTimeMillis()}" }
                    }
                    .plus(".mp3")

            // Use internal app directory
            val destinationFile =
                context.getExternalFilesDir(null)?.resolve("podcasts/$safeFilename")
                    ?: throw IllegalStateException("Cannot access app directory")

            // Create podcasts directory if it doesn't exist
            destinationFile.parentFile?.mkdirs()

            val request =
                DownloadManager.Request(audioUrl.toUri()).apply {
                    setTitle(articleTitle)
                    setDescription("Downloading podcast audio...")
                    setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    setDestinationUri(destinationFile.toUri())
                }

            val downloadId = downloadManager.enqueue(request)
            Result.success(DownloadResult(downloadId, destinationFile.absolutePath))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
