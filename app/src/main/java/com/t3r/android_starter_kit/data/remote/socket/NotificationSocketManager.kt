package com.t3r.android_starter_kit.data.remote.socket

import com.t3r.android_starter_kit.BuildConfig
import com.t3r.android_starter_kit.data.local.DataStoreManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages a Socket.IO connection to the `/notifications` namespace.
 *
 * Connects only while the user is authenticated and the app is in the foreground.
 * Emits [NotificationSocketEvent]s that ViewModels can collect to update UI in real-time.
 *
 * This is the mobile equivalent of the web admin's `useNotificationsSocket` composable.
 * FCM handles delivery when the app is in the background; this socket handles
 * live UI sync while the app is active.
 */
@Singleton
class NotificationSocketManager @Inject constructor(
    private val dataStoreManager: DataStoreManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null

    private val _events = MutableSharedFlow<NotificationSocketEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<NotificationSocketEvent> = _events.asSharedFlow()

    /**
     * Connect to the notifications WebSocket.
     * Call from Activity.onStart (foreground only).
     */
    fun connect() {
        if (socket?.connected() == true) return

        scope.launch {
            val token = dataStoreManager.getAccessToken()
            if (token.isNullOrBlank()) {
                Timber.d("[WS] No token available, skipping socket connection")
                return@launch
            }

            try {
                val baseUrl = BuildConfig.API_BASE_URL
                    .removeSuffix("/")
                    .replace(Regex("/api/v\\d+/?$"), "")

                val options = IO.Options().apply {
                    auth = mapOf("token" to token)
                    transports = arrayOf("websocket")
                    reconnectionAttempts = 5
                    reconnectionDelay = 2_000
                }

                val newSocket = IO.socket("$baseUrl/notifications", options)

                newSocket.on(Socket.EVENT_CONNECT) {
                    Timber.d("[WS] Notifications socket connected")
                }

                newSocket.on("notification") { args ->
                    handleNotificationEvent(args)
                }

                newSocket.on(Socket.EVENT_DISCONNECT) { args ->
                    val reason = args.firstOrNull()?.toString() ?: "unknown"
                    Timber.d("[WS] Notifications socket disconnected: $reason")
                }

                newSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    val error = args.firstOrNull()?.toString() ?: "unknown"
                    Timber.w("[WS] Notifications socket connection error: $error")
                }

                newSocket.connect()
                socket = newSocket
            } catch (e: Exception) {
                Timber.e(e, "[WS] Failed to create socket connection")
            }
        }
    }

    /**
     * Disconnect from the notifications WebSocket.
     * Call from Activity.onStop or on logout.
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Timber.d("[WS] Socket disconnected and cleaned up")
    }

    private fun handleNotificationEvent(args: Array<Any>) {
        try {
            val payload = args.firstOrNull()

            if (payload is JSONObject) {
                val event = payload.optString("event", "")
                val notificationId = payload.optString("notificationId", "")
                    .ifEmpty { null }
                val unreadCount = payload.optInt("unreadCount", -1)
                    .takeIf { it >= 0 }

                when (event) {
                    "read" -> _events.tryEmit(
                        NotificationSocketEvent.Read(notificationId, unreadCount),
                    )
                    "read_all" -> _events.tryEmit(
                        NotificationSocketEvent.ReadAll(unreadCount ?: 0),
                    )
                    "removed" -> _events.tryEmit(
                        NotificationSocketEvent.Removed(notificationId, unreadCount),
                    )
                    else -> {
                        // New notification (no event field) or broadcast
                        _events.tryEmit(NotificationSocketEvent.NewNotification)
                    }
                }
            } else {
                // Payload is not a JSONObject — treat as new notification
                _events.tryEmit(NotificationSocketEvent.NewNotification)
            }
        } catch (e: Exception) {
            Timber.e(e, "[WS] Failed to parse notification event")
            _events.tryEmit(NotificationSocketEvent.NewNotification)
        }
    }
}
