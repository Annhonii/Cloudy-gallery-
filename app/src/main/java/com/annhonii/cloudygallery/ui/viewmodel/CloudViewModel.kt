package com.annhonii.cloudygallery.ui.viewmodel

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatus
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatusEntity
import com.annhonii.cloudygallery.domain.model.CloudMediaItem
import com.annhonii.cloudygallery.domain.usecase.DownloadCloudMediaUseCase
import com.annhonii.cloudygallery.domain.usecase.GetCloudMediaUseCase
import com.annhonii.cloudygallery.domain.usecase.UploadMediaUseCase
import com.annhonii.cloudygallery.domain.repository.CloudRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

sealed class CloudUiState {
    object Loading : CloudUiState()
    data class Success(val media: List<CloudMediaItem>) : CloudUiState()
    data class Error(val message: String) : CloudUiState()
}

data class UploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val lastUploadedFileName: String? = null
)

@HiltViewModel
class CloudViewModel @Inject constructor(
    private val getCloudMediaUseCase: GetCloudMediaUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val downloadCloudMediaUseCase: DownloadCloudMediaUseCase,
    private val cloudRepository: CloudRepository,
    @Named("encrypted_prefs") private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<CloudUiState>(CloudUiState.Loading)
    val uiState: StateFlow<CloudUiState> = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatusEntity?>(null)
    val syncStatus: StateFlow<SyncStatusEntity?> = _syncStatus.asStateFlow()

    private val _isBotConnected = MutableStateFlow(false)
    val isBotConnected: StateFlow<Boolean> = _isBotConnected.asStateFlow()

    init {
        loadCloudMedia()
        observeSyncStatus()
        _isBotConnected.value = getBotToken().isNotEmpty() && getChatId().isNotEmpty()
    }

    private fun loadCloudMedia() {
        viewModelScope.launch {
            getCloudMediaUseCase()
                .catch { e ->
                    _uiState.value = CloudUiState.Error(e.message ?: "Failed to load cloud media")
                }
                .collect { media ->
                    _uiState.value = CloudUiState.Success(media)
                }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            cloudRepository.getSyncStatus()
                .catch { }
                .collect { status ->
                    _syncStatus.value = status
                }
        }
    }

    fun uploadMedia(uri: Uri, fileName: String) {
        val masterPassword = getMasterPassword()
        val botToken = getBotToken()
        val chatId = getChatId()

        if (masterPassword.isEmpty()) {
            _uploadState.value = UploadState(error = "Master password not set. Go to Settings.")
            return
        }
        if (botToken.isEmpty() || chatId.isEmpty()) {
            _uploadState.value = UploadState(error = "Telegram bot not configured. Go to Settings.")
            return
        }

        viewModelScope.launch {
            _uploadState.value = UploadState(isUploading = true)
            val result = uploadMediaUseCase(uri, fileName, masterPassword, botToken, chatId)
            if (result.isSuccess) {
                _uploadState.value = UploadState(
                    isUploading = false,
                    progress = 1f,
                    lastUploadedFileName = fileName
                )
            } else {
                _uploadState.value = UploadState(
                    isUploading = false,
                    error = result.exceptionOrNull()?.message ?: "Upload failed"
                )
            }
        }
    }

    fun downloadMedia(cloudMediaId: String, onResult: (ByteArray?) -> Unit) {
        val masterPassword = getMasterPassword()
        val botToken = getBotToken()

        if (masterPassword.isEmpty() || botToken.isEmpty()) {
            onResult(null)
            return
        }

        viewModelScope.launch {
            val result = downloadCloudMediaUseCase(cloudMediaId, masterPassword, botToken)
            onResult(result.getOrNull())
        }
    }

    fun clearUploadError() {
        _uploadState.value = _uploadState.value.copy(error = null)
    }

    fun markOnboardingComplete() {
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }

    fun saveSettings(masterPassword: String, botToken: String, chatId: String) {
        prefs.edit()
            .apply {
                if (masterPassword.isNotEmpty()) putString("master_password", masterPassword)
                putString("telegram_bot_token", botToken)
                putString("telegram_chat_id", chatId)
            }
            .apply()
        _isBotConnected.value = botToken.isNotEmpty() && chatId.isNotEmpty()
    }

    fun getBotToken(): String = prefs.getString("telegram_bot_token", "") ?: ""
    fun getChatId(): String = prefs.getString("telegram_chat_id", "") ?: ""
    fun getMasterPassword(): String = prefs.getString("master_password", "") ?: ""
}
