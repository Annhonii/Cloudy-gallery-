package com.annhonii.cloudygallery.domain.usecase

import com.annhonii.cloudygallery.domain.model.CloudMediaItem
import com.annhonii.cloudygallery.domain.repository.CloudRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCloudMediaUseCase @Inject constructor(
    private val repository: CloudRepository
) {
    operator fun invoke(): Flow<List<CloudMediaItem>> = repository.getCloudMedia()
}
