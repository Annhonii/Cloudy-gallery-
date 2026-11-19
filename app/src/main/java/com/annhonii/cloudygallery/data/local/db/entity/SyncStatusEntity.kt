package com.annhonii.cloudygallery.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus {
    IDLE,
    SYNCING,
    ERROR
}

@Entity(tableName = "sync_status")
data class SyncStatusEntity(
    @PrimaryKey
    val id: Int = 1, // singleton row
    val lastSyncAt: Long = 0L,
    val totalFiles: Int = 0,
    val syncedFiles: Int = 0,
    val status: SyncStatus = SyncStatus.IDLE,
    val errorMessage: String? = null
)
