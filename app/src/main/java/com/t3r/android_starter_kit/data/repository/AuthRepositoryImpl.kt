package com.t3r.android_starter_kit.data.repository

import com.t3r.android_starter_kit.core.network.safeApiCall
import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.core.result.map
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.mapper.toDomain
import com.t3r.android_starter_kit.data.remote.api.AccountApi
import com.t3r.android_starter_kit.data.remote.api.AuthApi
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.dto.auth.*
import com.t3r.android_starter_kit.data.remote.dto.notifications.RegisterDeviceRequestDto
import com.t3r.android_starter_kit.domain.model.*
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val accountApi: AccountApi,
    private val notificationsApi: NotificationsApi,
    private val dataStore: DataStoreManager,
) : AuthRepository {

    private suspend fun registerFcmDevice() {
        val fcmToken = dataStore.fcmToken.first() ?: return
        try {
            notificationsApi.registerDevice(
                RegisterDeviceRequestDto(token = fcmToken, platform = "android")
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to register FCM device")
        }
    }

    private suspend fun unregisterFcmDevice() {
        val fcmToken = dataStore.fcmToken.first() ?: return
        try {
            notificationsApi.unregisterDevice(fcmToken)
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister FCM device")
        }
    }

    override suspend fun login(identifier: String, password: String): Result<LoginResult> =
        safeApiCall {
            authApi.login(LoginRequestDto(identifier, password))
        }.map { response ->
            if (response.requiresTwoFactor) {
                LoginResult.TwoFactorRequired(
                    challengeToken = response.challengeToken ?: "",
                )
            } else {
                val tokens = AuthTokens(response.accessToken!!, response.refreshToken!!)
                dataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                response.user?.let { dataStore.saveUserId(it.id) }
                registerFcmDevice()
                LoginResult.Authenticated(
                    tokens = tokens,
                    user = response.user!!.toDomain(),
                    rules = response.rules?.map { it.toDomain() } ?: emptyList(),
                )
            }
        }

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        firstName: String?,
        lastName: String?,
    ): Result<RegisterResult> = safeApiCall {
        authApi.register(RegisterRequestDto(email, username, password, firstName, lastName))
    }.map { response ->
        if (response.accessToken != null) {
            val tokens = AuthTokens(response.accessToken, response.refreshToken!!)
            dataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
            response.user?.let { dataStore.saveUserId(it.id) }
            registerFcmDevice()
            RegisterResult.Authenticated(
                tokens = tokens,
                user = response.user!!.toDomain(),
            )
        } else {
            RegisterResult.VerificationRequired(
                message = response.message ?: "Please check your email to verify your account.",
            )
        }
    }

    override suspend fun verify2fa(
        challengeToken: String,
        code: String,
    ): Result<LoginResult.Authenticated> = safeApiCall {
        authApi.verify2fa(Verify2faRequestDto(challengeToken, code))
    }.map { response ->
        val tokens = AuthTokens(response.accessToken!!, response.refreshToken!!)
        dataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
        response.user?.let { dataStore.saveUserId(it.id) }
        registerFcmDevice()
        LoginResult.Authenticated(
            tokens = tokens,
            user = response.user!!.toDomain(),
            rules = response.rules?.map { it.toDomain() } ?: emptyList(),
        )
    }

    override suspend fun refreshToken(): Result<AuthTokens> {
        val currentRefreshToken = dataStore.refreshToken.first()
        return safeApiCall {
            authApi.refreshToken(RefreshTokenRequestDto(currentRefreshToken))
        }.map { response ->
            val tokens = AuthTokens(response.accessToken, response.refreshToken)
            dataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
            tokens
        }
    }

    override suspend fun logout(): Result<Unit> {
        val refreshToken = dataStore.refreshToken.first()
        unregisterFcmDevice()
        val result = safeApiCall {
            accountApi.logout(LogoutRequestDto(refreshToken))
        }
        dataStore.clearSession()
        return result.map { }
    }

    override suspend fun forgotPassword(email: String): Result<String> = safeApiCall {
        authApi.forgotPassword(ForgotPasswordRequestDto(email))
    }.map { it.message }

    override suspend fun resetPassword(token: String, password: String): Result<String> =
        safeApiCall {
            authApi.resetPassword(ResetPasswordRequestDto(token, password))
        }.map { it.message }

    override suspend fun verifyEmail(token: String): Result<String> = safeApiCall {
        authApi.verifyEmail(VerifyEmailRequestDto(token))
    }.map { it.message }

    override suspend fun resendVerification(email: String): Result<String> = safeApiCall {
        authApi.resendVerification(ForgotPasswordRequestDto(email))
    }.map { it.message }

    override suspend fun getMe(): Result<User> = safeApiCall {
        accountApi.getMe()
    }.map { it.user.toDomain() }

    override suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        email: String?,
        phoneNumber: String?,
    ): Result<User> = safeApiCall {
        accountApi.updateProfile(UpdateProfileRequestDto(firstName, lastName, email, phoneNumber))
    }.map { it.user.toDomain() }

    override suspend fun setAvatar(fileId: String): Result<Unit> = safeApiCall {
        accountApi.setAvatar(SetAvatarRequestDto(fileId))
    }.map { }

    override suspend fun deleteAvatar(): Result<Unit> = safeApiCall {
        accountApi.deleteAvatar()
    }

    override suspend fun deleteAccount(password: String): Result<Unit> = safeApiCall {
        accountApi.deleteAccount(DeleteAccountRequestDto(password))
    }.map { dataStore.clearSession() }

    override suspend fun setup2fa(): Result<TwoFactorSetup> = safeApiCall {
        accountApi.setup2fa()
    }.map { it.toDomain() }

    override suspend fun enable2fa(code: String): Result<List<String>> = safeApiCall {
        accountApi.enable2fa(Enable2faRequestDto(code))
    }.map { it.toDomain() }

    override suspend fun disable2fa(password: String, code: String): Result<String> = safeApiCall {
        accountApi.disable2fa(Disable2faRequestDto(password, code))
    }.map { it.message }

    override suspend fun isLoggedIn(): Boolean = dataStore.isLoggedIn.first()

    override suspend fun clearSession() = dataStore.clearSession()
}
