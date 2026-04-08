package com.t3r.android_starter_kit.data.remote.dto.settings

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

@Serializable
data class UpdateUserSettingsRequestDto(
    val language: String? = null,
    val theme: String? = null,
    val notificationsEnabled: Boolean? = null,
    val privacyLevel: String? = null,
)
