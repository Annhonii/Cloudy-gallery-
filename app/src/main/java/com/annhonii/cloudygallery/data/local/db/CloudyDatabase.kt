package com.annhonii.cloudygallery.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.annhonii.cloudygallery.data.local.db.dao.CloudMediaDao
import com.annhonii.cloudygallery.data.local.db.dao.MediaDao
import com.annhonii.cloudygallery.data.local.db.entity.CloudMediaEntity
import com.annhonii.cloudygallery.data.local.db.entity.MediaEntity
import com.annhonii.cloudygallery.data.local.db.entity.SyncStatusEntity

@Database(
    entities = [
        MediaEntity::class,
        CloudMediaEntity::class,
        SyncStatusEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CloudyDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun cloudMediaDao(): CloudMediaDao
}
