package com.annhonii.cloudygallery.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatus
import com.annhonii.cloudygallery.domain.model.CloudMediaItem
import com.annhonii.cloudygallery.ui.viewmodel.CloudUiState
import com.annhonii.cloudygallery.ui.viewmodel.CloudViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    viewModel: CloudViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val isBotConnected by viewModel.isBotConnected.collectAsStateWithLifecycle()

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uploadState.error) {
        uploadState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUploadError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CLOUD SYNC",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                },
                actions = {
                    // Connection status indicator
                    Icon(
                        imageVector = if (isBotConnected) Icons.Filled.Cloud else Icons.Filled.CloudOff,
                        contentDescription = if (isBotConnected) "Connected" else "Disconnected",
                        tint = if (isBotConnected) Color.White else Color.Gray,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sync status bar
            SyncStatusBar(
                syncStatus = syncStatus?.status ?: SyncStatus.IDLE,
                lastSyncAt = syncStatus?.lastSyncAt ?: 0L
            )

            // Upload progress
            AnimatedVisibility(
                visible = uploadState.isUploading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color(0xFF1A1A1A)
                )
            }

            HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)

            when (val state = uiState) {
                is CloudUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is CloudUiState.Error -> {
                    CloudEmptyState(message = state.message)
                }

                is CloudUiState.Success -> {
                    if (state.media.isEmpty()) {
                        CloudEmptyState(
                            message = if (!isBotConnected)
                                "Connect your Telegram bot in Settings to start syncing"
                            else
                                "No cloud photos yet. Upload from your gallery!"
                        )
                    } else {
                        CloudMediaGrid(media = state.media)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusBar(syncStatus: SyncStatus, lastSyncAt: Long) {
    val statusText = when (syncStatus) {
        SyncStatus.IDLE -> if (lastSyncAt > 0L) {
            "LAST SYNC: ${SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date(lastSyncAt))}"
        } else {
            "NOT SYNCED"
        }
        SyncStatus.SYNCING -> "SYNCING..."
        SyncStatus.ERROR -> "SYNC ERROR"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F0F))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (syncStatus == SyncStatus.SYNCING) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = Color.White,
                strokeWidth = 1.5.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = statusText,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = when (syncStatus) {
                SyncStatus.ERROR -> Color(0xFFCF6679)
                SyncStatus.SYNCING -> Color.White
                else -> Color.Gray
            }
        )
    }
}

@Composable
private fun CloudMediaGrid(media: List<CloudMediaItem>) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(media, key = { it.id }) { item ->
            CloudMediaCard(item = item)
        }
    }
}

@Composable
private fun CloudMediaCard(item: CloudMediaItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Cloud,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.fileName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ENC ◆ AES-256-GCM",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color.Gray
            )
            Text(
                text = formatFileSize(item.fileSize),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun CloudEmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Cloud,
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
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }
}
