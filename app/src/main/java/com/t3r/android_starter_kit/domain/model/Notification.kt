package com.t3r.android_starter_kit.domain.model

data class Notification(
    val id: String,
    val key: String?,
    val data: Map<String, String>?,
    val title: String?,
    val message: String?,
    val type: NotificationType,
    val isRead: Boolean,
    val link: String?,
    val createdAt: String?,
)

enum class NotificationType {
    GENERAL, INFO, WARNING, SUCCESS, ERROR;

    companion object {
        fun fromString(value: String): NotificationType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: GENERAL
    }
}
