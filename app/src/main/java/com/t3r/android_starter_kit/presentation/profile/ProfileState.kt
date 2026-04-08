package com.t3r.android_starter_kit.presentation.profile

import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.domain.model.User

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: AppError? = null,
    val isLoggingOut: Boolean = false,
    val loggedOut: Boolean = false,
)

sealed interface ProfileEvent {
    data object LoadProfile : ProfileEvent
    data object Logout : ProfileEvent
}
