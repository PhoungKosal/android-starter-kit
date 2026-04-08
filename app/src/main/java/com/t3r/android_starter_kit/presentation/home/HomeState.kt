package com.t3r.android_starter_kit.presentation.home

import com.starterkit.app.core.result.AppError
import com.starterkit.app.domain.model.User

data class HomeState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val unreadNotificationCount: Int = 0,
    val error: AppError? = null,
)

sealed interface HomeEvent {
    data object LoadProfile : HomeEvent
    data object Refresh : HomeEvent
}
