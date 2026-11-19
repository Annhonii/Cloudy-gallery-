package com.annhonii.cloudygallery.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MediaThumbnail(
    uri: String,
    isVideo: Boolean = false,
    isCloudItem: Boolean = false,
    isEncrypted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = uri,
            contentDescription = if (isVideo) "Video thumbnail" else "Photo thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(0.dp))
        )

        // Video play icon
        if (isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Cloud encryption badge
        AnimatedVisibility(
            visible = isCloudItem,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        if (isEncrypted) Color.Black.copy(alpha = 0.8f) else Color(0xFF1A1A1A).copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Cloud,
                    contentDescription = if (isEncrypted) "Encrypted cloud" else "Cloud",
                    tint = if (isEncrypted) Color.White else Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
