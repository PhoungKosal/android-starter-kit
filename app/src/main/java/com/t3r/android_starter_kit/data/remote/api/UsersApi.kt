package com.t3r.android_starter_kit.data.remote.api

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
