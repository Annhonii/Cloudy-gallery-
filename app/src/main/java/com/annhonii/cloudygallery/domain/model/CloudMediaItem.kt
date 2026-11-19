package com.annhonii.cloudygallery.domain.model

data class CloudMediaItem(
    val id: String,
    val telegramFileId: String,
    val fileName: String,
    val encryptionSalt: String,
    val isDownloaded: Boolean,
    val localBinPath: String?,
    val thumbnailPath: String?,
    val uploadedAt: Long,
    val fileSize: Long
)
