package com.t3r.android_starter_kit.presentation.auth

import com.t3r.android_starter_kit.core.result.AppError

data class AuthState(
    val isLoading: Boolean = false,
    val error: AppError? = null,

    // Login
    val identifier: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,

    // Register
    val email: String = "",
    val username: String = "",
    val registerPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",

    // 2FA
    val requiresTwoFactor: Boolean = false,
    val challengeToken: String = "",
    val twoFactorCode: String = "",

    // Forgot password
    val forgotPasswordEmail: String = "",
    val forgotPasswordSent: Boolean = false,

    // Message feedback
    val successMessage: String? = null,
)

sealed interface AuthEvent {
    // Login events
    data class UpdateIdentifier(val value: String) : AuthEvent
    data class UpdatePassword(val value: String) : AuthEvent
    data object TogglePasswordVisibility : AuthEvent
    data object Login : AuthEvent

    // Register events
    data class UpdateEmail(val value: String) : AuthEvent
    data class UpdateUsername(val value: String) : AuthEvent
    data class UpdateRegisterPassword(val value: String) : AuthEvent
    data class UpdateFirstName(val value: String) : AuthEvent
    data class UpdateLastName(val value: String) : AuthEvent
    data object Register : AuthEvent

    // 2FA events
    data class UpdateTwoFactorCode(val value: String) : AuthEvent
    data object VerifyTwoFactor : AuthEvent

    // Forgot password events
    data class UpdateForgotPasswordEmail(val value: String) : AuthEvent
    data object SendForgotPassword : AuthEvent

    // Common
    data object ClearError : AuthEvent
    data object ClearSuccessMessage : AuthEvent
}
