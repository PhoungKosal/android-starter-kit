package com.t3r.android_starter_kit.data.remote.api

import com.t3r.android_starter_kit.data.remote.dto.auth.DeleteAccountRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Disable2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Enable2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Enable2faResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.ForgotPasswordRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LoginRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LoginResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LogoutRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.MeResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RefreshTokenResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RegisterRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.RegisterResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.ResetPasswordRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.SetAvatarRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Setup2faResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UpdateProfileRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UserDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Verify2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.VerifyEmailRequestDto
import com.t3r.android_starter_kit.data.remote.dto.common.MessageResponseDto
import com.t3r.android_starter_kit.data.remote.dto.files.FileDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): RefreshTokenResponseDto

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): MessageResponseDto

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

    @POST("auth/2fa/setup")
    suspend fun setup2fa(@Header("Authorization") token: String): Setup2faResponseDto

    @POST("auth/2fa/enable")
    suspend fun enable2fa(
        @Header("Authorization") token: String,
        @Body request: Enable2faRequestDto,
    ): Enable2faResponseDto

    @HTTP(method = "DELETE", path = "auth/2fa/disable", hasBody = true)
    suspend fun disable2fa(
        @Header("Authorization") token: String,
        @Body request: Disable2faRequestDto,
    ): MessageResponseDto

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): MeResponseDto

    @PATCH("auth/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequestDto,
    ): UserDto

    @POST("auth/me/avatar")
    suspend fun setAvatar(
        @Header("Authorization") token: String,
        @Body request: SetAvatarRequestDto,
    ): FileDto

    @DELETE("auth/me/avatar")
    suspend fun deleteAvatar(@Header("Authorization") token: String)

    @GET("auth/me/files")
    suspend fun getMyFiles(@Header("Authorization") token: String): List<FileDto>

    @HTTP(method = "DELETE", path = "auth/me/account", hasBody = true)
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body request: DeleteAccountRequestDto,
    ): MessageResponseDto
}