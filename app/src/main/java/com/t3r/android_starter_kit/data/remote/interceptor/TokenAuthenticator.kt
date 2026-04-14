package com.t3r.android_starter_kit.data.remote.interceptor

import com.t3r.android_starter_kit.BuildConfig
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenResponseDto
import com.t3r.android_starter_kit.presentation.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
 * - Emits [sessionExpired] when the refresh token is invalid/expired
 *   so the UI layer can navigate to login cleanly
 * - Circuit breaker: once refresh fails with an auth error, subsequent
 *   401s return null immediately without retrying until [resetCircuitBreaker]
 *   is called (typically after a successful login)
 */
class TokenAuthenticator(
    private val publicClient: OkHttpClient,
    private val dataStoreManager: DataStoreManager,
    private val json: Json,
) : Authenticator {

    private val lock = Any()

    /** True when refresh has failed with an auth error — prevents retry storms. */
    @Volatile
    private var refreshFailed = false

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    /** Reset the circuit breaker after a successful login. */
    fun resetCircuitBreaker() {
        refreshFailed = false
        Timber.d("Token refresh circuit breaker reset")
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Circuit breaker — refresh already failed, don't retry
        if (refreshFailed) return null

        // Prevent infinite retry loops
        if (responseCount(response) > 1) return null

        synchronized(lock) {
            // Re-check inside the lock (another thread may have reset it)
            if (refreshFailed) return null

            val currentToken = dataStoreManager.getAccessToken()
            val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // Another thread already refreshed the token — just retry with it
            if (currentToken != null && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = dataStoreManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                onRefreshFailed()
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

                refreshResponse.use { res ->
                    if (res.isSuccessful) {
                        val responseBody = res.body.string()
                        val tokens = json.decodeFromString<RefreshTokenResponseDto>(responseBody)
                        runBlocking { dataStoreManager.saveTokens(tokens.accessToken, tokens.refreshToken) }

                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${tokens.accessToken}")
                            .build()
                    } else {
                        Timber.w("Token refresh returned ${res.code}")
                        onRefreshFailed()
                        null
                    }
                }
            } catch (e: java.io.IOException) {
                // Network errors are transient — don't trip the circuit breaker
                Timber.e(e, "Token refresh network error")
                null
            } catch (e: Exception) {
                Timber.e(e, "Token refresh failed")
                onRefreshFailed()
                null
            }
        }
    }

    private fun onRefreshFailed() {
        refreshFailed = true
        Timber.w("Token refresh failed — circuit breaker tripped")
        runBlocking { dataStoreManager.clearSession() }
        SettingsViewModel.clearCache()
        _sessionExpired.tryEmit(Unit)
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
