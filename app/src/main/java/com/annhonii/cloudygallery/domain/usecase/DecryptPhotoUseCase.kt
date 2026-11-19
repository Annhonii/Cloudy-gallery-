package com.annhonii.cloudygallery.domain.usecase

import com.annhonii.cloudygallery.crypto.EncryptionManager
import javax.inject.Inject

class DecryptPhotoUseCase @Inject constructor(
    private val encryptionManager: EncryptionManager
) {
    operator fun invoke(binBytes: ByteArray, masterPassword: String): ByteArray {
        return encryptionManager.decryptPhoto(binBytes, masterPassword)
    }
}
