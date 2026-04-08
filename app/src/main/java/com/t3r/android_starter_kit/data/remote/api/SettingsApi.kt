package com.t3r.android_starter_kit.data.remote.api

import com.t3r.android_starter_kit.data.remote.dto.settings.SystemSettingDto
import com.t3r.android_starter_kit.data.remote.dto.settings.UpdateSettingRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface SettingsApi {

    @GET("settings")
    suspend fun getSettings(): List<SystemSettingDto>

    @GET("settings/{key}")
    suspend fun getSetting(@Path("key") key: String): SystemSettingDto

    @PATCH("settings/{key}")
    suspend fun updateSetting(
        @Path("key") key: String,
        @Body request: UpdateSettingRequestDto,
    ): SystemSettingDto
}