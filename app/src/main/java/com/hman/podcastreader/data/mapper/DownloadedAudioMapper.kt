package com.hman.podcastreader.data.mapper

import com.hman.podcastreader.data.dataSource.local.entity.DownloadedAudioEntity
import com.hman.podcastreader.domain.model.DownloadedAudio

fun DownloadedAudioEntity.toDomain(): DownloadedAudio {
    return DownloadedAudio(
            id = id,
            articleTitle = articleTitle,
            audioFilePath = audioFilePath,
            downloadedAt = downloadedAt
    )
}

fun DownloadedAudio.toEntity(): DownloadedAudioEntity {
    return DownloadedAudioEntity(
            id = id,
            articleTitle = articleTitle,
            audioFilePath = audioFilePath,
            downloadedAt = downloadedAt
    )
}
