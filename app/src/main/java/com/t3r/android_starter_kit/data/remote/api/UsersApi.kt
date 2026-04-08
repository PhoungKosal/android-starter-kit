package com.t3r.android_starter_kit.data.remote.api

import com.t3r.android_starter_kit.data.remote.dto.auth.UserDto
import com.t3r.android_starter_kit.data.remote.dto.auth.UserSettingsDto
import com.t3r.android_starter_kit.data.remote.dto.common.PaginatedResponseDto
import com.t3r.android_starter_kit.data.remote.dto.settings.UpdateUserSettingsRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface UsersApi {

    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("sortBy") sortBy: String? = null,
    ): PaginatedResponseDto<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: Map<String, String>,
    ): UserDto

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String)

    @GET("users/{id}/settings")
    suspend fun getUserSettings(@Path("id") id: String): UserSettingsDto

    @PATCH("users/{id}/settings")
    suspend fun updateUserSettings(
        @Path("id") id: String,
        @Body request: UpdateUserSettingsRequestDto,
    ): UserSettingsDto
}
