package com.annhonii.cloudygallery.telegram

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.annhonii.cloudygallery.data.local.db.dao.CloudMediaDao
import com.annhonii.cloudygallery.data.local.db.entity.CloudMediaEntity
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatus
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatusEntity
import com.annhonii.cloudygallery.data.remote.TelegramApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Named

@HiltWorker
class BotSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val telegramApi: TelegramApi,
    private val cloudMediaDao: CloudMediaDao,
    @Named("encrypted_prefs") private val prefs: SharedPreferences
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "BotSyncWorker"
        const val WORK_NAME = "bot_sync_periodic"
        const val KEY_LAST_UPDATE_ID = "last_update_id"

        fun enqueuePeriodicSync(context: Context) {
            val request = PeriodicWorkRequestBuilder<BotSyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val botToken = prefs.getString("telegram_bot_token", "") ?: ""
        val chatId = prefs.getString("telegram_chat_id", "") ?: ""

        if (botToken.isEmpty() || chatId.isEmpty()) {
            Log.d(TAG, "Bot not configured, skipping sync")
            return Result.success()
        }

        return try {
            Log.d(TAG, "Starting sync...")
            cloudMediaDao.upsertSyncStatus(
                SyncStatusEntity(status = SyncStatus.SYNCING)
            )

            val lastUpdateId = prefs.getLong(KEY_LAST_UPDATE_ID, 0L)
            val updatesResult = telegramApi.getUpdates(
                botToken = botToken,
                offset = if (lastUpdateId > 0) lastUpdateId + 1 else 0
            )

            if (updatesResult.isFailure) {
                Log.e(TAG, "getUpdates failed: ${updatesResult.exceptionOrNull()?.message}")
                cloudMediaDao.upsertSyncStatus(
                    SyncStatusEntity(
                        status = SyncStatus.ERROR,
                        errorMessage = updatesResult.exceptionOrNull()?.message
                    )
                )
                return Result.retry()
            }

            val updates = updatesResult.getOrThrow()
            var newMaxUpdateId = lastUpdateId
            var newFilesFound = 0

            for (update in updates) {
                if (update.updateId > newMaxUpdateId) newMaxUpdateId = update.updateId

                val message = update.message ?: update.channelPost ?: continue
                val document = message.document ?: continue

                // Only process .bin files with CGAL caption
                val caption = message.caption ?: ""
                val fileName = if (caption.startsWith("CGAL:")) {
                    caption.removePrefix("CGAL:")
                } else {
                    document.fileName ?: continue
                }

                if (!fileName.isNotEmpty()) continue

                // Check if already tracked
                val existing = cloudMediaDao.getByTelegramFileId(document.fileId)
                if (existing != null) continue

                // New cloud file found — register it
                val entity = CloudMediaEntity(
                    id = UUID.randomUUID().toString(),
                    telegramFileId = document.fileId,
                    fileName = fileName,
                    encryptionSalt = "", // Will be read from bin header on download
                    isDownloaded = false,
                    fileSize = document.fileSize ?: 0L
                )
                cloudMediaDao.insertCloudMedia(entity)
                newFilesFound++
                Log.d(TAG, "Registered new cloud file: $fileName (fileId=${document.fileId})")
            }

            if (newMaxUpdateId > lastUpdateId) {
                prefs.edit().putLong(KEY_LAST_UPDATE_ID, newMaxUpdateId).apply()
            }

            val totalFiles = cloudMediaDao.getCloudMediaCount()
            cloudMediaDao.upsertSyncStatus(
                SyncStatusEntity(
                    status = SyncStatus.IDLE,
                    lastSyncAt = System.currentTimeMillis(),
                    totalFiles = totalFiles,
                    syncedFiles = totalFiles
                )
            )

            Log.d(TAG, "Sync complete: $newFilesFound new files, $totalFiles total")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            cloudMediaDao.upsertSyncStatus(
                SyncStatusEntity(status = SyncStatus.ERROR, errorMessage = e.message)
            )
            Result.retry()
        }
    }
}
