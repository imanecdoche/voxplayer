package com.vox.music.core.storage.di

import com.vox.music.core.storage.repository.AudioAnalysisRepository
import com.vox.music.core.storage.repository.AudioAnalysisRepositoryImpl
import com.vox.music.core.storage.repository.AudioRepository
import com.vox.music.core.storage.repository.AudioRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository

    @Binds
    @Singleton
    abstract fun bindAudioAnalysisRepository(
        impl: AudioAnalysisRepositoryImpl
    ): AudioAnalysisRepository
}
