package com.t3r.android_starter_kit.di

import com.t3r.android_starter_kit.data.remote.api.AuthApi
import com.t3r.android_starter_kit.data.remote.api.FilesApi
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.api.SettingsApi
import com.t3r.android_starter_kit.data.remote.api.UsersApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(@PublicClient retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(@AuthenticatedClient retrofit: Retrofit): UsersApi =
        retrofit.create(UsersApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationsApi(@AuthenticatedClient retrofit: Retrofit): NotificationsApi =
        retrofit.create(NotificationsApi::class.java)

    @Provides
    @Singleton
    fun provideFilesApi(@AuthenticatedClient retrofit: Retrofit): FilesApi =
        retrofit.create(FilesApi::class.java)

    @Provides
    @Singleton
    fun provideSettingsApi(@AuthenticatedClient retrofit: Retrofit): SettingsApi =
        retrofit.create(SettingsApi::class.java)
}
