package com.annhonii.cloudygallery.di

import com.annhonii.cloudygallery.data.repository.CloudRepositoryImpl
import com.annhonii.cloudygallery.data.repository.MediaRepositoryImpl
import com.annhonii.cloudygallery.domain.repository.CloudRepository
import com.annhonii.cloudygallery.domain.repository.MediaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindCloudRepository(impl: CloudRepositoryImpl): CloudRepository
}
