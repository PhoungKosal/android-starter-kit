package com.t3r.android_starter_kit.data.remote.dto.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val key: String? = null,
    val data: Map<String, String>? = null,
    val title: String? = null,
    val message: String? = null,
    val type: String = "GENERAL",
    val isRead: Boolean = false,
    val readAt: String? = null,
    val link: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class UnreadCountDto(
    val count: Int,
)

@Serializable
data class RegisterDeviceRequestDto(
    val token: String,
    val platform: String = "android",
    val appVersion: String? = null,
)

@Serializable
data class NotificationActionRequestDto(
    val notificationId: String? = null,
    val action: String,
)
