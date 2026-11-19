package com.annhonii.cloudygallery.data.repository

import android.util.Log
import com.annhonii.cloudygallery.crypto.BinFileManager
import com.annhonii.cloudygallery.crypto.EncryptionManager
import com.annhonii.cloudygallery.data.local.db.dao.CloudMediaDao
import com.annhonii.cloudygallery.data.local.db.entity.CloudMediaEntity
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatus
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatusEntity
import com.annhonii.cloudygallery.data.remote.TelegramApi
import com.annhonii.cloudygallery.domain.model.CloudMediaItem
import com.annhonii.cloudygallery.domain.repository.CloudRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudRepositoryImpl @Inject constructor(
    private val telegramApi: TelegramApi,
    private val cloudMediaDao: CloudMediaDao,
    private val encryptionManager: EncryptionManager,
    private val binFileManager: BinFileManager
) : CloudRepository {

    companion object {
        private const val TAG = "CloudRepository"
    }

    override fun getCloudMedia(): Flow<List<CloudMediaItem>> {
        return cloudMediaDao.getAllCloudMedia().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getSyncStatus(): Flow<SyncStatusEntity?> {
        return cloudMediaDao.getSyncStatus()
    }

    override suspend fun uploadMedia(
        photoBytes: ByteArray,
        fileName: String,
        masterPassword: String,
        botToken: String,
        chatId: String
    ): Result<CloudMediaItem> {
        return try {
            Log.d(TAG, "Encrypting and uploading: $fileName")

            // Update sync status
            cloudMediaDao.upsertSyncStatus(
                SyncStatusEntity(status = SyncStatus.SYNCING)
            )

            // Encrypt
            val binBytes = encryptionManager.encryptPhoto(photoBytes, masterPassword)
            val binFileName = "${UUID.randomUUID()}.bin"

            // Upload to Telegram
            val uploadResult = telegramApi.sendDocument(
                botToken = botToken,
                chatId = chatId,
                fileName = binFileName,
                fileBytes = binBytes,
                caption = "CGAL:$fileName"
            )

            if (uploadResult.isFailure) {
                cloudMediaDao.upsertSyncStatus(
                    SyncStatusEntity(
                        status = SyncStatus.ERROR,
                        errorMessage = uploadResult.exceptionOrNull()?.message
                    )
                )
                return Result.failure(uploadResult.exceptionOrNull()!!)
            }

            val message = uploadResult.getOrThrow()
            val document = message.document
                ?: return Result.failure(Exception("No document in Telegram response"))

            // Extract salt from the bin file header for reference
            val saltHex = binBytes.copyOfRange(
                EncryptionManager.MAGIC_SIZE,
                EncryptionManager.MAGIC_SIZE + EncryptionManager.SALT_SIZE
            ).toHex()

            // Store locally
            val entity = CloudMediaEntity(
                id = UUID.randomUUID().toString(),
                telegramFileId = document.fileId,
                fileName = fileName,
                encryptionSalt = saltHex,
                isDownloaded = false,
                localBinPath = null,
                fileSize = document.fileSize ?: binBytes.size.toLong()
            )
            cloudMediaDao.insertCloudMedia(entity)

            // Update sync status
            cloudMediaDao.upsertSyncStatus(
                SyncStatusEntity(status = SyncStatus.IDLE, lastSyncAt = System.currentTimeMillis())
            )

            Log.d(TAG, "Upload complete: ${entity.id}")
            Result.success(entity.toModel())
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            cloudMediaDao.upsertSyncStatus(
                SyncStatusEntity(status = SyncStatus.ERROR, errorMessage = e.message)
            )
            Result.failure(e)
        }
    }

    override suspend fun downloadMedia(
        cloudMediaId: String,
        masterPassword: String,
        botToken: String
    ): Result<ByteArray> {
        return try {
            val entity = cloudMediaDao.getCloudMediaById(cloudMediaId)
                ?: return Result.failure(Exception("Cloud media not found: $cloudMediaId"))

            // Check if already downloaded
            if (entity.isDownloaded && entity.localBinPath != null) {
                val existing = binFileManager.loadBinFile(entity.localBinPath)
                if (existing != null) {
                    Log.d(TAG, "Using cached bin file: ${entity.localBinPath}")
                    return Result.success(encryptionManager.decryptPhoto(existing, masterPassword))
                }
            }

            // Download from Telegram
            Log.d(TAG, "Downloading from Telegram: ${entity.telegramFileId}")
            val downloadResult = telegramApi.downloadFile(botToken, entity.telegramFileId)

            if (downloadResult.isFailure) {
                return Result.failure(downloadResult.exceptionOrNull()!!)
            }

            val binBytes = downloadResult.getOrThrow()

            // Save locally
            val localPath = binFileManager.saveBinFile(entity.id, binBytes)
            cloudMediaDao.updateDownloadStatus(entity.id, true, localPath)

            // Decrypt
            val decrypted = encryptionManager.decryptPhoto(binBytes, masterPassword)
            Log.d(TAG, "Download and decrypt success: ${decrypted.size} bytes")

            Result.success(decrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCloudMedia(cloudMediaId: String) {
        val entity = cloudMediaDao.getCloudMediaById(cloudMediaId) ?: return
        entity.localBinPath?.let { binFileManager.deleteBinFile(it) }
        cloudMediaDao.deleteCloudMediaById(cloudMediaId)
    }

    private fun CloudMediaEntity.toModel() = CloudMediaItem(
        id = id,
        telegramFileId = telegramFileId,
        fileName = fileName,
        encryptionSalt = encryptionSalt,
        isDownloaded = isDownloaded,
        localBinPath = localBinPath,
        thumbnailPath = thumbnailPath,
        uploadedAt = uploadedAt,
        fileSize = fileSize
    )

    private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }
}
