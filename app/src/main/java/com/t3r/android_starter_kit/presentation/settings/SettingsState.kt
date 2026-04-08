package com.t3r.android_starter_kit.presentation.settings

import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.domain.model.User

data class SettingsState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: AppError? = null,
)

sealed interface SettingsEvent {
    data object Load : SettingsEvent
}
