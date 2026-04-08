package com.t3r.android_starter_kit.data.remote.api

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

    @POST("auth/verify-2fa")
    suspend fun verify2fa(@Body request: Verify2faRequestDto): LoginResponseDto

    @POST("auth/enable-2fa")
    suspend fun enable2fa(@Body request: Enable2faRequestDto): Enable2faResponseDto

    @POST("auth/disable-2fa")
    suspend fun disable2fa(@Body request: Disable2faRequestDto): MessageResponseDto

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
    ): UserDto

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