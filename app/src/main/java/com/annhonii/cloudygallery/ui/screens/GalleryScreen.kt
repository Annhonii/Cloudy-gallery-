package com.annhonii.cloudygallery.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.annhonii.cloudygallery.domain.model.MediaItem
import com.annhonii.cloudygallery.ui.components.MediaThumbnail
import com.annhonii.cloudygallery.ui.viewmodel.MediaUiState
import com.annhonii.cloudygallery.ui.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onMediaClick: (Long) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()

    // Check onboarding
    LaunchedEffect(Unit) {
        if (!viewModel.isOnboarded()) {
            onNavigateToOnboarding()
        }
    }

    // Permission request
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CLOUDY GALLERY",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MediaUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is MediaUiState.Error -> {
                    EmptyGalleryState(
                        message = state.message,
                        onRetry = {
                            permissionLauncher.launch(permissions)
                        }
                    )
                }

                is MediaUiState.Success -> {
                    if (state.media.isEmpty()) {
                        EmptyGalleryState(
                            message = "No photos or videos found",
                            onRetry = null
                        )
                    } else {
                        MediaGrid(
                            media = state.media,
                            onMediaClick = onMediaClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGrid(
    media: List<MediaItem>,
    onMediaClick: (Long) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalItemSpacing = 2.dp
    ) {
        itemsIndexed(
            items = media,
            key = { _, item -> item.id }
        ) { index, item ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(item.id) { visible = true }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(200, delayMillis = (index % 12) * 20)) +
                        slideInVertically(tween(200, delayMillis = (index % 12) * 20)) { it / 4 }
            ) {
                val aspectRatio = if (item.width > 0 && item.height > 0) {
                    item.width.toFloat() / item.height.toFloat()
                } else 1f

                MediaThumbnail(
                    uri = item.uri,
                    isVideo = item.isVideo,
                    isCloudItem = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio.coerceIn(0.5f, 2f))
                        .clickable { onMediaClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyGalleryState(
    message: String,
    onRetry: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
            ) {
                Text("GRANT PERMISSION", fontFamily = FontFamily.Monospace)
            }
        }
    }
}
