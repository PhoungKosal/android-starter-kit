package com.t3r.android_starter_kit.navigation


import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using kotlinx.serialization.
 * Each route is a data class/object representing a destination.
 */
sealed interface Route : NavKey {

    // Auth routes
    @Serializable data object Login : Route
    @Serializable data object Register : Route
    @Serializable data object ForgotPassword : Route
    @Serializable data object TwoFactor : Route

    // Main app routes
    @Serializable data object Home : Route
    @Serializable data object Profile : Route
    @Serializable data object Notifications : Route

    // Settings
    @Serializable data object Settings : Route
}
