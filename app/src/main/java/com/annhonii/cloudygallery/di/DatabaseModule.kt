package com.annhonii.cloudygallery.di

import android.content.Context
import androidx.room.Room
import com.annhonii.cloudygallery.data.local.db.CloudyDatabase
import com.annhonii.cloudygallery.data.local.db.dao.CloudMediaDao
import com.annhonii.cloudygallery.data.local.db.dao.MediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCloudyDatabase(@ApplicationContext context: Context): CloudyDatabase {
        return Room.databaseBuilder(
            context,
            CloudyDatabase::class.java,
            "cloudy_gallery.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaDao(database: CloudyDatabase): MediaDao {
        return database.mediaDao()
    }

    @Provides
    @Singleton
    fun provideCloudMediaDao(database: CloudyDatabase): CloudMediaDao {
        return database.cloudMediaDao()
    }
}
