package com.t3r.android_starter_kit.data.remote.dto.auth

import kotlinx.serialization.Serializable

// -- Requests --

@Serializable
data class LoginRequestDto(
    val identifier: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val username: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String? = null,
)

@Serializable
data class LogoutRequestDto(
    val refreshToken: String? = null,
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)

@Serializable
data class ResetPasswordRequestDto(
    val token: String,
    val password: String,
)

@Serializable
data class VerifyEmailRequestDto(
    val token: String,
)

@Serializable
data class Verify2faRequestDto(
    val challengeToken: String,
    val code: String,
)

@Serializable
data class Enable2faRequestDto(
    val code: String,
)

@Serializable
data class Disable2faRequestDto(
    val password: String,
    val code: String,
)

@Serializable
data class UpdateProfileRequestDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
)

@Serializable
data class SetAvatarRequestDto(
    val fileId: String,
)

@Serializable
data class DeleteAccountRequestDto(
    val password: String,
)

// -- Responses --

@Serializable
data class LoginResponseDto(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val rules: List<RuleDto>? = null,
    val requiresTwoFactor: Boolean = false,
    val challengeToken: String? = null,
)

@Serializable
data class RegisterResponseDto(
    val user: UserDto? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val rules: List<RuleDto>? = null,
    val message: String? = null,
)

@Serializable
data class RefreshTokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class Setup2faResponseDto(
    val qrCode: String,
    val manualEntryKey: String,
)

@Serializable
data class Enable2faResponseDto(
    val message: String = "",
    val recoveryCodes: List<String> = emptyList(),
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = false,
    val lastLoginAt: String? = null,
    val twoFactorEnabled: Boolean = false,
    val avatarUrl: String? = null,
    val profile: UserProfileDto? = null,
    val settings: UserSettingsDto? = null,
    val role: RoleDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class UserProfileDto(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val location: String? = null,
)

@Serializable
data class UserSettingsDto(
    val id: String? = null,
    val language: String? = null,
    val theme: String? = null,
    val notificationsEnabled: Boolean = true,
    val privacyLevel: String? = null,
)

@Serializable
data class RoleDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val permissions: List<PermissionDto>? = null,
)

@Serializable
data class PermissionDto(
    val id: String,
    val action: String,
    val subject: String,
    val conditions: Map<String, String>? = null,
    val key: String? = null,
    val description: String? = null,
)

@Serializable
data class RuleDto(
    val action: String,
    val subject: String,
    val conditions: Map<String, String>? = null,
    val inverted: Boolean = false,
)

@Serializable
data class MeResponseDto(
    val user: UserDto,
    val rules: List<RuleDto>? = null,
)
