package com.annhonii.cloudygallery.domain.repository

import com.annhonii.cloudygallery.data.local.db.entity.SyncStatusEntity
import com.annhonii.cloudygallery.domain.model.CloudMediaItem
import kotlinx.coroutines.flow.Flow

interface CloudRepository {
    fun getCloudMedia(): Flow<List<CloudMediaItem>>
    fun getSyncStatus(): Flow<SyncStatusEntity?>
    suspend fun uploadMedia(
        photoBytes: ByteArray,
        fileName: String,
        masterPassword: String,
        botToken: String,
        chatId: String
    ): Result<CloudMediaItem>
    suspend fun downloadMedia(
        cloudMediaId: String,
        masterPassword: String,
        botToken: String
    ): Result<ByteArray>
    suspend fun deleteCloudMedia(cloudMediaId: String)
}
