package com.t3r.android_starter_kit.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.t3r.android_starter_kit.core.network.safeApiCall
import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.core.result.map
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.mapper.toDomain
import com.t3r.android_starter_kit.data.remote.api.AccountApi
import com.t3r.android_starter_kit.data.remote.api.AuthApi
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.api.UsersApi
import com.t3r.android_starter_kit.data.remote.dto.auth.DeleteAccountRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Disable2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Enable2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.ForgotPasswordRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LoginRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LogoutRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RegisterRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.ResetPasswordRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.SetAvatarRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UpdateProfileRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Verify2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.VerifyEmailRequestDto
import com.t3r.android_starter_kit.data.remote.dto.notifications.RegisterDeviceRequestDto
import com.t3r.android_starter_kit.data.remote.dto.settings.UpdateUserSettingsRequestDto
import com.t3r.android_starter_kit.data.remote.interceptor.TokenAuthenticator
import com.t3r.android_starter_kit.domain.model.AuthTokens
import com.t3r.android_starter_kit.domain.model.LoginResult
import com.t3r.android_starter_kit.domain.model.RegisterResult
import com.t3r.android_starter_kit.domain.model.TwoFactorSetup
import com.t3r.android_starter_kit.domain.model.User
import com.t3r.android_starter_kit.domain.model.UserSettings
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val accountApi: AccountApi,
    private val notificationsApi: NotificationsApi,
    private val usersApi: UsersApi,
    private val dataStore: DataStoreManager,
    private val tokenAuthenticator: TokenAuthenticator,
) : AuthRepository {

    private suspend fun registerFcmDevice() {
        val fcmToken = dataStore.fcmToken.first()
            ?: try {
                val token = suspendCancellableCoroutine { cont ->
                    FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                dataStore.saveFcmToken(token)
                token
            } catch (e: Exception) {
                Timber.e(e, "Failed to retrieve FCM token from Firebase")
                return
            }
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
                val accessToken = requireNotNull(response.accessToken) { "Login response missing accessToken" }
                val refreshToken = requireNotNull(response.refreshToken) { "Login response missing refreshToken" }
                val user = requireNotNull(response.user) { "Login response missing user" }
                val tokens = AuthTokens(accessToken, refreshToken)
                dataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                dataStore.saveUserId(user.id)
                tokenAuthenticator.resetCircuitBreaker()
                registerFcmDevice()
                LoginResult.Authenticated(
                    tokens = tokens,
                    user = user.toDomain(),
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
            val refreshToken = requireNotNull(response.refreshToken) { "Register response missing refreshToken" }
            val user = requireNotNull(response.user) { "Register response missing user" }
            val tokens = AuthTokens(response.accessToken, refreshToken)
            dataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
            dataStore.saveUserId(user.id)
            tokenAuthenticator.resetCircuitBreaker()
            registerFcmDevice()
            RegisterResult.Authenticated(
                tokens = tokens,
                user = user.toDomain(),
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
        val accessToken = requireNotNull(response.accessToken) { "2FA response missing accessToken" }
        val refreshToken = requireNotNull(response.refreshToken) { "2FA response missing refreshToken" }
        val user = requireNotNull(response.user) { "2FA response missing user" }
        val tokens = AuthTokens(accessToken, refreshToken)
        dataStore.saveTokens(tokens.accessToken, tokens.refreshToken)
        dataStore.saveUserId(user.id)
        tokenAuthenticator.resetCircuitBreaker()
        registerFcmDevice()
        LoginResult.Authenticated(
            tokens = tokens,
            user = user.toDomain(),
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

    override suspend fun getMySettings(): Result<UserSettings> = safeApiCall {
        usersApi.getMySettings()
    }.map { it.toDomain() }

    override suspend fun updateMySettings(
        language: String?,
        theme: String?,
        timezone: String?,
        dateFormat: String?,
        primaryColor: String?,
        neutralColor: String?,
        emailNotifications: Boolean?,
        pushNotifications: Boolean?,
    ): Result<UserSettings> = safeApiCall {
        usersApi.updateMySettings(
            request = UpdateUserSettingsRequestDto(
                language = language,
                theme = theme,
                timezone = timezone,
                dateFormat = dateFormat,
                primaryColor = primaryColor,
                neutralColor = neutralColor,
                emailNotifications = emailNotifications,
                pushNotifications = pushNotifications,
            ),
        )
    }.map { it.toDomain() }
}
