package com.t3r.android_starter_kit.domain.model

/** Auth tokens pair. */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

/** Login result — could require 2FA. */
sealed interface LoginResult {
    data class Authenticated(
        val tokens: AuthTokens,
        val user: User,
        val rules: List<CaslRule>,
    ) : LoginResult

    data class TwoFactorRequired(
        val challengeToken: String,
    ) : LoginResult
}

/** Registration result — may require email verification. */
sealed interface RegisterResult {
    data class Authenticated(
        val tokens: AuthTokens,
        val user: User,
    ) : RegisterResult

    data class VerificationRequired(
        val message: String,
    ) : RegisterResult
}
