package com.t3r.android_starter_kit.domain.model

/** Domain model for authenticated user. */
data class User(
    val id: String,
    val email: String,
    val username: String,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val twoFactorEnabled: Boolean,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val avatar: String?,
    val bio: String?,
    val location: String?,
    val role: Role?,
    val language: String?,
    val theme: String?,
    val notificationsEnabled: Boolean,
    val createdAt: String?,
) {
    val displayName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .ifBlank { username }

    val initials: String
        get() = displayName
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
}

data class Role(
    val id: String,
    val name: String,
    val description: String?,
    val permissions: List<Permission>,
)

data class Permission(
    val id: String,
    val action: String,
    val subject: String,
    val key: String?,
)

data class CaslRule(
    val action: String,
    val subject: String,
    val conditions: Map<String, String>?,
    val inverted: Boolean,
)
