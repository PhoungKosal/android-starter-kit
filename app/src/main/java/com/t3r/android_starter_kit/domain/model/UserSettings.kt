package com.t3r.android_starter_kit.domain.model

/** Domain model for user settings — matches GET/PATCH /users/settings. */
data class UserSettings(
    val theme: String,
    val language: String,
    val timezone: String,
    val dateFormat: String,
    val primaryColor: String,
    val neutralColor: String,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
)
