package com.annhonii.cloudygallery.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.annhonii.cloudygallery.data.local.db.dao.MediaDao
import com.annhonii.cloudygallery.data.local.db.entity.MediaEntity
import com.annhonii.cloudygallery.domain.model.MediaItem
import com.annhonii.cloudygallery.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaDao: MediaDao
) : MediaRepository {

    companion object {
        private const val TAG = "MediaRepository"
    }

    override fun getLocalMedia(): Flow<List<MediaItem>> {
        // Observe MediaStore changes via ContentObserver + callbackFlow
        return callbackFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    launch { send(queryMediaStore()) }
                }
            }
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
            context.contentResolver.registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )

            // Initial load
            send(queryMediaStore())

            awaitClose {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    override suspend fun getMediaById(id: Long): MediaItem? {
        return queryMediaStoreById(id)
    }

    private fun queryMediaStore(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()

        // Query images
        items.addAll(queryCollection(
            collection = if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            },
            isVideo = false
        ))

        // Query videos
        items.addAll(queryCollection(
            collection = if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            },
            isVideo = true
        ))

        return items.sortedByDescending { it.dateTaken }
    }

    private fun queryCollection(collection: Uri, isVideo: Boolean): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT
        )

        val sortOrder = "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
        val items = mutableListOf<MediaItem>()

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    items.add(
                        MediaItem(
                            id = id,
                            uri = contentUri.toString(),
                            name = cursor.getString(nameCol) ?: "",
                            dateTaken = cursor.getLong(dateCol),
                            mimeType = cursor.getString(mimeCol) ?: "",
                            size = cursor.getLong(sizeCol),
                            width = cursor.getInt(widthCol),
                            height = cursor.getInt(heightCol),
                            isVideo = isVideo
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }

        return items
    }

    private fun queryMediaStoreById(id: Long): MediaItem? {
        // Try images first, then videos
        return queryCollection(
            if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            },
            false
        ).find { it.id == id } ?: queryCollection(
            if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            },
            true
        ).find { it.id == id }
    }
}
