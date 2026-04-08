package com.t3r.android_starter_kit.domain.repository

import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.domain.model.*

interface AuthRepository {

    suspend fun login(identifier: String, password: String): Result<LoginResult>

    suspend fun register(
        email: String,
        username: String,
        password: String,
        firstName: String? = null,
        lastName: String? = null,
    ): Result<RegisterResult>

    suspend fun verify2fa(challengeToken: String, code: String): Result<LoginResult.Authenticated>

    suspend fun refreshToken(): Result<AuthTokens>

    suspend fun logout(): Result<Unit>

    suspend fun forgotPassword(email: String): Result<String>

    suspend fun resetPassword(token: String, password: String): Result<String>

    suspend fun verifyEmail(token: String): Result<String>

    suspend fun resendVerification(email: String): Result<String>

    suspend fun getMe(): Result<User>

    suspend fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
    ): Result<User>

    suspend fun setAvatar(fileId: String): Result<Unit>

    suspend fun deleteAvatar(): Result<Unit>

    suspend fun deleteAccount(password: String): Result<Unit>

    suspend fun setup2fa(): Result<TwoFactorSetup>

    suspend fun enable2fa(code: String): Result<List<String>>

    suspend fun disable2fa(password: String, code: String): Result<String>

    suspend fun isLoggedIn(): Boolean

    suspend fun clearSession()
}
