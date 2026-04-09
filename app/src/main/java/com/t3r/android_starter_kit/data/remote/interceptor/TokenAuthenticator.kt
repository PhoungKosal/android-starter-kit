package com.t3r.android_starter_kit.data.remote.interceptor

import com.t3r.android_starter_kit.BuildConfig
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenResponseDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

/**
 * OkHttp Authenticator that handles 401 responses by refreshing the
 * access token using the stored refresh token.
 *
 * - Uses the [publicClient] (no auth interceptor) to call POST /auth/refresh
 * - Synchronized to prevent multiple concurrent refresh calls
 * - Clears the session when the refresh token is invalid/expired
 */
class TokenAuthenticator(
    private val publicClient: OkHttpClient,
    private val dataStoreManager: DataStoreManager,
    private val json: Json,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops
        if (responseCount(response) > 1) return null

        synchronized(lock) {
            val currentToken = runBlocking { dataStoreManager.accessToken.first() }
            val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // Another thread already refreshed the token — just retry with it
            if (currentToken != null && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = runBlocking { dataStoreManager.refreshToken.first() }
            if (refreshToken.isNullOrBlank()) {
                runBlocking { dataStoreManager.clearSession() }
                return null
            }

            return try {
                val body = json.encodeToString(
                    RefreshTokenRequestDto.serializer(),
                    RefreshTokenRequestDto(refreshToken),
                )
                val request = Request.Builder()
                    .url(BuildConfig.API_BASE_URL + "auth/refresh")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val refreshResponse = publicClient.newCall(request).execute()

                if (refreshResponse.isSuccessful) {
                    val responseBody = refreshResponse.body?.string()
                    if (responseBody == null) {
                        runBlocking { dataStoreManager.clearSession() }
                        return null
                    }
                    val tokens = json.decodeFromString<RefreshTokenResponseDto>(responseBody)
                    runBlocking { dataStoreManager.saveTokens(tokens.accessToken, tokens.refreshToken) }

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokens.accessToken}")
                        .build()
                } else {
                    Timber.w("Token refresh returned ${refreshResponse.code}")
                    runBlocking { dataStoreManager.clearSession() }
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Token refresh failed")
                // Don't clear session on network errors — might be transient
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
