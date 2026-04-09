package com.t3r.android_starter_kit.data.remote.api

import com.t3r.android_starter_kit.data.remote.dto.auth.ForgotPasswordRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LoginRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LoginResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RegisterRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RegisterResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.ResetPasswordRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Verify2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.VerifyEmailRequestDto
import com.t3r.android_starter_kit.data.remote.dto.common.MessageResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Public auth endpoints that do not require a bearer token.
 * Uses the PublicClient Retrofit instance.
 */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): RefreshTokenResponseDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): MessageResponseDto

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): MessageResponseDto

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto): MessageResponseDto

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ForgotPasswordRequestDto): MessageResponseDto

    @POST("auth/2fa/verify")
    suspend fun verify2fa(@Body request: Verify2faRequestDto): LoginResponseDto
}