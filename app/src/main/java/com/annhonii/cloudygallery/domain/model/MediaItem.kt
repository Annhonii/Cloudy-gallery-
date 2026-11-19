package com.annhonii.cloudygallery.domain.model

data class MediaItem(
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
