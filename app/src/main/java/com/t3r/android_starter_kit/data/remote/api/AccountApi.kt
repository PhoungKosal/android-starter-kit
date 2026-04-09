package com.t3r.android_starter_kit.data.remote.api

import com.t3r.android_starter_kit.data.remote.dto.auth.DeleteAccountRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Disable2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Enable2faRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Enable2faResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.LogoutRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.MeResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.SetAvatarRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.Setup2faResponseDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UpdateProfileRequestDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UpdateProfileResponseDto
import com.t3r.android_starter_kit.data.remote.dto.common.MessageResponseDto
import com.t3r.android_starter_kit.data.remote.dto.files.FileDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Authenticated account endpoints.
 * Uses the AuthenticatedClient so the bearer token and
 * automatic refresh are handled by interceptors.
 */
interface AccountApi {

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): MessageResponseDto

    @GET("auth/me")
    suspend fun getMe(): MeResponseDto

    @PATCH("auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): UpdateProfileResponseDto

    @POST("auth/me/avatar")
    suspend fun setAvatar(@Body request: SetAvatarRequestDto): FileDto

    @DELETE("auth/me/avatar")
    suspend fun deleteAvatar()

    @GET("auth/me/files")
    suspend fun getMyFiles(): List<FileDto>

    @HTTP(method = "DELETE", path = "auth/me/account", hasBody = true)
    suspend fun deleteAccount(@Body request: DeleteAccountRequestDto): MessageResponseDto

    @POST("auth/2fa/setup")
    suspend fun setup2fa(): Setup2faResponseDto

    @POST("auth/2fa/enable")
    suspend fun enable2fa(@Body request: Enable2faRequestDto): Enable2faResponseDto

    @HTTP(method = "DELETE", path = "auth/2fa/disable", hasBody = true)
    suspend fun disable2fa(@Body request: Disable2faRequestDto): MessageResponseDto
}
