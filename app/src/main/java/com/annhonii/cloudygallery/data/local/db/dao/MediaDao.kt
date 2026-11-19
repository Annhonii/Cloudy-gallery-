package com.annhonii.cloudygallery.data.local.db.dao

import androidx.room.*
import com.annhonii.cloudygallery.data.local.db.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Query("SELECT * FROM media ORDER BY dateTaken DESC")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaEntity?

    @Query("SELECT * FROM media WHERE mimeType LIKE 'video/%' ORDER BY dateTaken DESC")
    fun getVideos(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE mimeType LIKE 'image/%' ORDER BY dateTaken DESC")
    fun getImages(): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(mediaList: List<MediaEntity>)

    @Update
    suspend fun updateMedia(media: MediaEntity)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM media")
    suspend fun deleteAllMedia()

    @Query("SELECT COUNT(*) FROM media")
    suspend fun getMediaCount(): Int

    @Query("SELECT * FROM media WHERE name LIKE '%' || :query || '%' ORDER BY dateTaken DESC")
    fun searchMedia(query: String): Flow<List<MediaEntity>>
}
