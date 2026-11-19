package com.annhonii.cloudygallery.domain.usecase

import com.annhonii.cloudygallery.domain.model.MediaItem
import com.annhonii.cloudygallery.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalMediaUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getLocalMedia()
}
