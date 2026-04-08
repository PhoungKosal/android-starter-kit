package com.t3r.android_starter_kit.di

import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.remote.api.AuthApi
import com.t3r.android_starter_kit.data.remote.api.FilesApi
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.api.UsersApi
import com.t3r.android_starter_kit.data.repository.AuthRepositoryImpl
import com.t3r.android_starter_kit.data.repository.FilesRepositoryImpl
import com.t3r.android_starter_kit.data.repository.NotificationsRepositoryImpl
import com.t3r.android_starter_kit.data.repository.UsersRepositoryImpl
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import com.t3r.android_starter_kit.domain.repository.FilesRepository
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import com.t3r.android_starter_kit.domain.repository.UsersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        dataStoreManager: DataStoreManager,
    ): AuthRepository = AuthRepositoryImpl(authApi, dataStoreManager)

    @Provides
    @Singleton
    fun provideUsersRepository(usersApi: UsersApi): UsersRepository =
        UsersRepositoryImpl(usersApi)

    @Provides
    @Singleton
    fun provideNotificationsRepository(notificationsApi: NotificationsApi): NotificationsRepository =
        NotificationsRepositoryImpl(notificationsApi)

    @Provides
    @Singleton
    fun provideFilesRepository(filesApi: FilesApi): FilesRepository =
        FilesRepositoryImpl(filesApi)
}
