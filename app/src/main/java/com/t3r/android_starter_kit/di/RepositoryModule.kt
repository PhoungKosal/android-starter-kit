package com.t3r.android_starter_kit.di


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
