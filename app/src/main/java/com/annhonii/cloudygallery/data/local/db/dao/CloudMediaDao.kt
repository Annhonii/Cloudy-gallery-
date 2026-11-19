package com.annhonii.cloudygallery.data.local.db.dao

import androidx.room.*
import com.annhonii.cloudygallery.data.local.db.entity.CloudMediaEntity
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudMediaDao {

    @Query("SELECT * FROM cloud_media ORDER BY uploadedAt DESC")
    fun getAllCloudMedia(): Flow<List<CloudMediaEntity>>

    @Query("SELECT * FROM cloud_media WHERE id = :id")
    suspend fun getCloudMediaById(id: String): CloudMediaEntity?

    @Query("SELECT * FROM cloud_media WHERE telegramFileId = :fileId")
    suspend fun getByTelegramFileId(fileId: String): CloudMediaEntity?

    @Query("SELECT * FROM cloud_media WHERE isDownloaded = 1 ORDER BY uploadedAt DESC")
    fun getDownloadedMedia(): Flow<List<CloudMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudMedia(entity: CloudMediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCloudMedia(entities: List<CloudMediaEntity>)

    @Update
    suspend fun updateCloudMedia(entity: CloudMediaEntity)

    @Delete
    suspend fun deleteCloudMedia(entity: CloudMediaEntity)

    @Query("DELETE FROM cloud_media WHERE id = :id")
    suspend fun deleteCloudMediaById(id: String)

    @Query("DELETE FROM cloud_media")
    suspend fun deleteAllCloudMedia()

    @Query("SELECT COUNT(*) FROM cloud_media")
    suspend fun getCloudMediaCount(): Int

    @Query("UPDATE cloud_media SET isDownloaded = :downloaded, localBinPath = :path WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, downloaded: Boolean, path: String?)

    // Sync status
    @Query("SELECT * FROM sync_status WHERE id = 1")
    fun getSyncStatus(): Flow<SyncStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncStatus(status: SyncStatusEntity)
}
