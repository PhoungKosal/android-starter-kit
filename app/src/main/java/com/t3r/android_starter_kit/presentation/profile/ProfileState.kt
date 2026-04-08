package com.t3r.android_starter_kit.presentation.profile

import com.starterkit.app.core.result.AppError
import com.starterkit.app.domain.model.User

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
