package com.annhonii.cloudygallery.domain.usecase

import android.content.Context
import android.net.Uri
import com.annhonii.cloudygallery.domain.model.CloudMediaItem
import com.annhonii.cloudygallery.domain.repository.CloudRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UploadMediaUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CloudRepository
) {
    suspend operator fun invoke(
        uri: Uri,
        fileName: String,
        masterPassword: String,
        botToken: String,
        chatId: String
    ): Result<CloudMediaItem> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("Cannot read file: $uri"))

            repository.uploadMedia(
                photoBytes = bytes,
                fileName = fileName,
                masterPassword = masterPassword,
                botToken = botToken,
                chatId = chatId
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
