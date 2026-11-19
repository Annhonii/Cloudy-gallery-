package com.annhonii.cloudygallery.data.remote

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class TelegramResponse<T>(
    val ok: Boolean,
    val result: T? = null,
    val description: String? = null
)

@Serializable
data class TelegramFile(
    @SerialName("file_id") val fileId: String,
    @SerialName("file_unique_id") val fileUniqueId: String,
    @SerialName("file_size") val fileSize: Long? = null,
    @SerialName("file_path") val filePath: String? = null
)

@Serializable
data class TelegramDocument(
    @SerialName("file_id") val fileId: String,
    @SerialName("file_unique_id") val fileUniqueId: String,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
    @SerialName("mime_type") val mimeType: String? = null
)

@Serializable
data class TelegramMessage(
    @SerialName("message_id") val messageId: Long,
    @SerialName("document") val document: TelegramDocument? = null,
    @SerialName("caption") val caption: String? = null
)

@Serializable
data class TelegramUpdate(
    @SerialName("update_id") val updateId: Long,
    @SerialName("message") val message: TelegramMessage? = null,
    @SerialName("channel_post") val channelPost: TelegramMessage? = null
)

/**
 * Ktor-based Telegram Bot API client for Cloudy Gallery.
 */
@Singleton
class TelegramApi @Inject constructor(
    private val httpClient: HttpClient
) {

    companion object {
        private const val TAG = "TelegramApi"
        private const val BASE_URL = "https://api.telegram.org"
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50 MB Telegram limit
    }

    /**
     * Uploads an encrypted .bin file to Telegram as a document.
     */
    suspend fun sendDocument(
        botToken: String,
        chatId: String,
        fileName: String,
        fileBytes: ByteArray,
        caption: String = ""
    ): Result<TelegramMessage> {
        return try {
            Log.d(TAG, "Uploading document: $fileName (${fileBytes.size} bytes)")

            val response: TelegramResponse<TelegramMessage> = httpClient.post(
                "$BASE_URL/bot$botToken/sendDocument"
            ) {
                setBody(MultiPartFormDataContent(formData {
                    append("chat_id", chatId)
                    if (caption.isNotEmpty()) append("caption", caption)
                    appendInput(
                        key = "document",
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, "application/octet-stream")
                        },
                        size = fileBytes.size.toLong()
                    ) {
                        io.ktor.utils.io.ByteReadChannel(fileBytes)
                    }
                }))
            }.body()

            if (response.ok && response.result != null) {
                Log.d(TAG, "Upload success: messageId=${response.result.messageId}")
                Result.success(response.result)
            } else {
                val error = response.description ?: "Unknown Telegram API error"
                Log.e(TAG, "Upload failed: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }

    /**
     * Polls for new updates (messages) from the bot.
     */
    suspend fun getUpdates(
        botToken: String,
        offset: Long = 0,
        limit: Int = 100,
        timeout: Int = 0
    ): Result<List<TelegramUpdate>> {
        return try {
            val response: TelegramResponse<List<TelegramUpdate>> = httpClient.get(
                "$BASE_URL/bot$botToken/getUpdates"
            ) {
                parameter("offset", offset)
                parameter("limit", limit)
                parameter("timeout", timeout)
            }.body()

            if (response.ok && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.description ?: "getUpdates failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUpdates exception", e)
            Result.failure(e)
        }
    }

    /**
     * Gets file info (including download path) for a file by fileId.
     */
    suspend fun getFile(botToken: String, fileId: String): Result<TelegramFile> {
        return try {
            val response: TelegramResponse<TelegramFile> = httpClient.get(
                "$BASE_URL/bot$botToken/getFile"
            ) {
                parameter("file_id", fileId)
            }.body()

            if (response.ok && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.description ?: "getFile failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFile exception", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads a file by fileId. Returns raw bytes of the .bin file.
     */
    suspend fun downloadFile(botToken: String, fileId: String): Result<ByteArray> {
        return try {
            val fileResult = getFile(botToken, fileId)
            if (fileResult.isFailure) {
                return Result.failure(fileResult.exceptionOrNull()!!)
            }

            val filePath = fileResult.getOrThrow().filePath
                ?: return Result.failure(Exception("No file_path returned"))

            Log.d(TAG, "Downloading file: $filePath")

            val bytes: ByteArray = httpClient.get(
                "$BASE_URL/file/bot$botToken/$filePath"
            ).readBytes()

            Log.d(TAG, "Downloaded ${bytes.size} bytes")
            Result.success(bytes)
        } catch (e: Exception) {
            Log.e(TAG, "downloadFile exception", e)
            Result.failure(e)
        }
    }

    /**
     * Tests bot token validity by calling getMe.
     */
    suspend fun testBotToken(botToken: String): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.get("$BASE_URL/bot$botToken/getMe")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
