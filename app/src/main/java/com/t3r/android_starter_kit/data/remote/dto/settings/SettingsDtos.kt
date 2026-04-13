package com.t3r.android_starter_kit.data.remote.dto.settings

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class SystemSettingDto(
    val id: String,
    val key: String,
    val value: String? = null,
    val type: String,
    val group: String,
    val isPublic: Boolean = false,
    val description: String? = null,
)

@Serializable
data class UpdateSettingRequestDto(
    val value: String?,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class UpdateUserSettingsRequestDto(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val theme: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val language: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val timezone: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val dateFormat: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val primaryColor: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val neutralColor: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val emailNotifications: Boolean? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val pushNotifications: Boolean? = null,
)
