package com.t3r.android_starter_kit.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {

    // Auth routes
    @Serializable data object Login : Route
    @Serializable data object Register : Route
    @Serializable data object ForgotPassword : Route
    @Serializable data class TwoFactor(val challengeToken: String = "") : Route
    @Serializable data class VerifyEmail(val email: String = "") : Route
    @Serializable data class ResetPassword(val token: String = "") : Route

    // Main app routes
    @Serializable data object Home : Route
    @Serializable data object Profile : Route
    @Serializable data object Notifications : Route

    // Settings
    @Serializable data object Settings : Route
    @Serializable data class TwoFactorSettings(val twoFactorEnabled: Boolean = false) : Route
}
