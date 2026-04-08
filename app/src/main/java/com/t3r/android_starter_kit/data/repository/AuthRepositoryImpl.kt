package com.t3r.android_starter_kit.data.repository

import com.starterkit.app.core.network.safeApiCall
import com.starterkit.app.core.result.Result
import com.starterkit.app.core.result.map
import com.starterkit.app.data.local.DataStoreManager
import com.starterkit.app.data.mapper.toDomain
import com.starterkit.app.data.remote.api.AuthApi
import com.starterkit.app.data.remote.dto.auth.*
import com.starterkit.app.domain.model.*
import com.starterkit.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val dataStore: DataStoreManager,
) : AuthRepository {

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
        val result = safeApiCall {
            authApi.logout(LogoutRequestDto(refreshToken))
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

    override suspend fun getMe(): Result<User> {
        val token = dataStore.accessToken.first() ?: return Result.Error(
            com.starterkit.app.core.result.AppError("UNAUTHORIZED", "Not authenticated")
        )
        return safeApiCall {
            authApi.getMe("Bearer $token")
        }.map { it.user.toDomain() }
    }

    override suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        email: String?,
        phoneNumber: String?,
    ): Result<User> {
        val token = dataStore.accessToken.first() ?: return Result.Error(
            com.starterkit.app.core.result.AppError("UNAUTHORIZED", "Not authenticated")
        )
        return safeApiCall {
            authApi.updateProfile(
                "Bearer $token",
                UpdateProfileRequestDto(firstName, lastName, email, phoneNumber),
            )
        }.map { it.toDomain() }
    }

    override suspend fun setAvatar(fileId: String): Result<User> {
        val token = dataStore.accessToken.first() ?: return Result.Error(
            com.starterkit.app.core.result.AppError("UNAUTHORIZED", "Not authenticated")
        )
        return safeApiCall {
            authApi.setAvatar("Bearer $token", SetAvatarRequestDto(fileId))
        }.map { it.toDomain() }
    }

    override suspend fun deleteAvatar(): Result<Unit> {
        val token = dataStore.accessToken.first() ?: return Result.Error(
            com.starterkit.app.core.result.AppError("UNAUTHORIZED", "Not authenticated")
        )
        return safeApiCall { authApi.deleteAvatar("Bearer $token") }
    }

    override suspend fun deleteAccount(password: String): Result<Unit> {
        val token = dataStore.accessToken.first() ?: return Result.Error(
            com.starterkit.app.core.result.AppError("UNAUTHORIZED", "Not authenticated")
        )
        return safeApiCall {
            authApi.deleteAccount("Bearer $token", DeleteAccountRequestDto(password))
        }.map {
            dataStore.clearSession()
        }
    }

    override suspend fun isLoggedIn(): Boolean = dataStore.isLoggedIn.first()

    override suspend fun clearSession() = dataStore.clearSession()
}
