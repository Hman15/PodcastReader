package com.hman.podcastreader.domain.model

data class DownloadedAudio(
    val id: String,
    val articleTitle: String,
    val audioFilePath: String,
    val downloadedAt: Long
)
