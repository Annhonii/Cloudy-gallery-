package com.annhonii.cloudygallery.domain.usecase

import com.annhonii.cloudygallery.crypto.EncryptionManager
import javax.inject.Inject

class EncryptPhotoUseCase @Inject constructor(
    private val encryptionManager: EncryptionManager
) {
    operator fun invoke(photoBytes: ByteArray, masterPassword: String): ByteArray {
        return encryptionManager.encryptPhoto(photoBytes, masterPassword)
    }
}
