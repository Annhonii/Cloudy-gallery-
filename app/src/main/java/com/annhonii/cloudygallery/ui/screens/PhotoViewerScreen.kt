package com.annhonii.cloudygallery.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.annhonii.cloudygallery.domain.model.MediaItem
import com.annhonii.cloudygallery.ui.viewmodel.MediaUiState
import com.annhonii.cloudygallery.ui.viewmodel.MediaViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    mediaId: Long,
    onBack: () -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val mediaItem = remember(uiState, mediaId) {
        (uiState as? MediaUiState.Success)?.media?.find { it.id == mediaId }
    }

    var showInfo by remember { mutableStateOf(false) }

    // Pinch-to-zoom state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        mediaItem?.let { item ->
            // Main image with pinch to zoom
            AsyncImage(
                model = item.uri,
                contentDescription = item.name,
                contentScale = if (scale <= 1f) ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            offsetX += pan.x * scale
                            offsetY += pan.y * scale
                            // Clamp pan when zoomed out
                            if (scale <= 1f) {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
            )
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Top bar overlay
        TopAppBar(
            title = {
                Text(
                    text = mediaItem?.name ?: "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { showInfo = !showInfo }) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Metadata overlay
        if (showInfo && mediaItem != null) {
            MetadataOverlay(
                item = mediaItem,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun MetadataOverlay(item: MediaItem, modifier: Modifier = Modifier) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        MetaRow("FILE", item.name)
        MetaRow("DATE", dateFormat.format(Date(item.dateTaken)))
        MetaRow("SIZE", formatSize(item.size))
        if (item.width > 0) MetaRow("DIMENSIONS", "${item.width} × ${item.height}")
        MetaRow("TYPE", item.mimeType)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color.White
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }
}
