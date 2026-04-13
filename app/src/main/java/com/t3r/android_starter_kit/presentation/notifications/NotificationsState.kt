package com.t3r.android_starter_kit.presentation.notifications

import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.domain.model.Notification

data class NotificationsState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val error: AppError? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
)

sealed interface NotificationsEvent {
    data object Refresh : NotificationsEvent
    data object LoadMore : NotificationsEvent
    data class MarkAsRead(val id: String) : NotificationsEvent
    data object MarkAllAsRead : NotificationsEvent
    data class Delete(val id: String) : NotificationsEvent
}
