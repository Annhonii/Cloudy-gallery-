package com.annhonii.cloudygallery.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhonii.cloudygallery.domain.model.MediaItem
import com.annhonii.cloudygallery.domain.usecase.GetLocalMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

sealed class MediaUiState {
    object Loading : MediaUiState()
    data class Success(val media: List<MediaItem>) : MediaUiState()
    data class Error(val message: String) : MediaUiState()
}

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val getLocalMediaUseCase: GetLocalMediaUseCase,
    @Named("encrypted_prefs") private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<MediaUiState>(MediaUiState.Loading)
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
    val selectedMedia: StateFlow<MediaItem?> = _selectedMedia.asStateFlow()

    fun onPermissionGranted() {
        _hasPermission.value = true
        loadMedia()
    }

    fun onPermissionDenied() {
        _hasPermission.value = false
        _uiState.value = MediaUiState.Error("Storage permission required to view gallery")
    }

    private fun loadMedia() {
        viewModelScope.launch {
            _uiState.value = MediaUiState.Loading
            getLocalMediaUseCase()
                .catch { e ->
                    _uiState.value = MediaUiState.Error(e.message ?: "Failed to load media")
                }
                .collect { media ->
                    _uiState.value = MediaUiState.Success(media)
                }
        }
    }

    fun selectMedia(media: MediaItem?) {
        _selectedMedia.value = media
    }

    fun isOnboarded(): Boolean {
        return prefs.getBoolean("onboarding_complete", false)
    }
}
