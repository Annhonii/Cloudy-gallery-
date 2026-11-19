package com.annhonii.cloudygallery.domain.repository

import com.annhonii.cloudygallery.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getLocalMedia(): Flow<List<MediaItem>>
    suspend fun getMediaById(id: Long): MediaItem?
}
