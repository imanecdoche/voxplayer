package com.vox.music.core.database.di

import android.content.Context
import androidx.room.Room
import com.vox.music.core.database.VoxDatabase
import com.vox.music.core.database.dao.AudioTrackDao
import com.vox.music.core.database.dao.PlaylistDao
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
    fun provideVoxDatabase(
        @ApplicationContext context: Context
    ): VoxDatabase {
        return Room.databaseBuilder(
            context,
            VoxDatabase::class.java,
            VoxDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAudioTrackDao(
        database: VoxDatabase
    ): AudioTrackDao {
        return database.audioTrackDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(
        database: VoxDatabase
    ): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideAudioAnalysisDao(
        database: VoxDatabase
    ): com.vox.music.core.database.dao.AudioAnalysisDao {
        return database.audioAnalysisDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(
        database: VoxDatabase
    ): com.vox.music.core.database.dao.SearchHistoryDao {
        return database.searchHistoryDao()
    }
}
