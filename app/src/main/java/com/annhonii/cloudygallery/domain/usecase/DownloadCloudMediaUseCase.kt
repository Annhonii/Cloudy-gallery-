package com.annhonii.cloudygallery.domain.usecase

import com.annhonii.cloudygallery.domain.repository.CloudRepository
import javax.inject.Inject

class DownloadCloudMediaUseCase @Inject constructor(
    private val repository: CloudRepository
) {
    suspend operator fun invoke(
        cloudMediaId: String,
        masterPassword: String,
        botToken: String
    ): Result<ByteArray> {
        return repository.downloadMedia(
            cloudMediaId = cloudMediaId,
            masterPassword = masterPassword,
            botToken = botToken
        )
    }
}
