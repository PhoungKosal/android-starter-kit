package com.t3r.android_starter_kit.data.remote.interceptor


import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


/**
 * OkHttp interceptor that attaches the JWT access token
 * to every authenticated API request.
 */
class AuthInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { dataStoreManager.accessToken.first() }
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
