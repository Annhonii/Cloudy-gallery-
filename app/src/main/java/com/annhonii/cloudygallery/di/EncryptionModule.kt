package com.annhonii.cloudygallery.di

import com.annhonii.cloudygallery.crypto.BinFileManager
import com.annhonii.cloudygallery.crypto.EncryptionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {
    // EncryptionManager and BinFileManager are @Singleton @Inject constructors
    // so Hilt handles them automatically; this module is a placeholder
    // for any additional encryption-related bindings.
}
