package com.t3r.android_starter_kit.presentation.settings

import com.t3r.android_starter_kit.R
import com.t3r.android_starter_kit.core.locale.LocaleManager
import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.domain.model.User
import com.t3r.android_starter_kit.domain.model.UserSettings

/**
 * Theme modes matching the backend `theme` field ("light" | "dark").
 */
enum class AppTheme(
    val value: String,
    val displayNameRes: Int,
    val icon: String,
) {
    LIGHT("light", R.string.settings_theme_light, "☀\uFE0F"),
    DARK("dark", R.string.settings_theme_dark, "\uD83C\uDF19");

    companion object {
        fun fromString(value: String?): AppTheme =
            entries.firstOrNull { it.value == value } ?: LIGHT
    }
}

data class SettingsState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val settings: UserSettings? = null,
    val error: AppError? = null,
    val currentLocale: LocaleManager.AppLocale = LocaleManager.getCurrentLocale(),
    val showLanguagePicker: Boolean = false,
    val currentTheme: AppTheme = AppTheme.LIGHT,
    val showThemePicker: Boolean = false,
    val isSaving: Boolean = false,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
)

sealed interface SettingsEvent {
    data object Load : SettingsEvent
    data object ShowLanguagePicker : SettingsEvent
    data object DismissLanguagePicker : SettingsEvent
    data class ChangeLocale(val locale: LocaleManager.AppLocale) : SettingsEvent
    data object ShowThemePicker : SettingsEvent
    data object DismissThemePicker : SettingsEvent
    data class ChangeTheme(val theme: AppTheme) : SettingsEvent
    data class ToggleEmailNotifications(val enabled: Boolean) : SettingsEvent
    data class TogglePushNotifications(val enabled: Boolean) : SettingsEvent
}
