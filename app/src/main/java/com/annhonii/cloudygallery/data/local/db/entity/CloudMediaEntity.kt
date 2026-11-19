package com.annhonii.cloudygallery.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloud_media")
data class CloudMediaEntity(
    @PrimaryKey
    val id: String, // UUID
    val telegramFileId: String,
    val fileName: String,
    val encryptionSalt: String, // hex string
    val isDownloaded: Boolean = false,
    val localBinPath: String? = null,
    val thumbnailPath: String? = null,
    val uploadedAt: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L
)
