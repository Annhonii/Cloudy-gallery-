package com.annhonii.cloudygallery.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey
    val id: Long,
    val uri: String,
    val name: String,
    val dateTaken: Long,
    val mimeType: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val isVideo: Boolean = false
)
