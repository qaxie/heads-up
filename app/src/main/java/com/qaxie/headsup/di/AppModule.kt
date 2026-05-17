package com.qaxie.headsup.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.qaxie.headsup.data.AppPreferenceRepository
import com.qaxie.headsup.data.AppPreferenceRepositoryImpl
import com.qaxie.headsup.data.SuppressedCountRepository
import com.qaxie.headsup.data.SuppressedCountRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("headsup_preferences") }
        )

    @Provides
    @Singleton
    fun provideAppPreferenceRepository(
        dataStore: DataStore<Preferences>
    ): AppPreferenceRepository = AppPreferenceRepositoryImpl(dataStore)

    @Provides
    @Singleton
    fun provideSuppressedCountRepository(
        dataStore: DataStore<Preferences>
    ): SuppressedCountRepository = SuppressedCountRepositoryImpl(dataStore)
}
