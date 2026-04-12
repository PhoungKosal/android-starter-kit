package com.t3r.android_starter_kit.data.remote.socket

/**
 * Events emitted by [NotificationSocketManager] when the backend pushes
 * state changes over the WebSocket.
 */
sealed interface NotificationSocketEvent {
    /** A brand-new notification arrived — reload the list and play sound. */
    data object NewNotification : NotificationSocketEvent

    /** A specific notification was marked as read. */
    data class Read(val notificationId: String?, val unreadCount: Int?) : NotificationSocketEvent

    /** All notifications were marked as read. */
    data class ReadAll(val unreadCount: Int) : NotificationSocketEvent

    /** A notification was deleted. */
    data class Removed(val notificationId: String?, val unreadCount: Int?) : NotificationSocketEvent
}
