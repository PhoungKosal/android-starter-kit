package com.t3r.android_starter_kit.data.remote.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.dto.notifications.NotificationActionRequestDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Handles notification action button taps (e.g. "Mark as Read", "Dismiss").
 *
 * Cancels the notification from the shade and forwards the action
 * to the backend via `POST /notifications/action`.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationsApi: NotificationsApi

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return
        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val notifyId = intent.getIntExtra(EXTRA_NOTIFY_ID, -1)

        Timber.d("Notification action received: action=$action, notificationId=$notificationId")

        // Dismiss the notification from the shade
        if (notifyId >= 0) {
            try {
                NotificationManagerCompat.from(context).cancel(notifyId)
            } catch (_: SecurityException) {
                Timber.w("Cannot cancel notification — permission not granted")
            }
        }

        // Fire-and-forget call to backend
        val pendingResult = goAsync()
        scope.launch {
            try {
                notificationsApi.handleAction(
                    NotificationActionRequestDto(
                        notificationId = notificationId,
                        action = action,
                    ),
                )
                Timber.d("Action '$action' sent to backend successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to send action '$action' to backend")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ACTION_NOTIFICATION = "com.t3r.android_starter_kit.NOTIFICATION_ACTION"
        private const val EXTRA_ACTION = "extra_action"
        private const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        private const val EXTRA_NOTIFY_ID = "extra_notify_id"

        /** Build an intent for a notification action button. */
        fun buildIntent(
            context: Context,
            action: String,
            notificationId: String?,
            notifyId: Int,
        ): Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = ACTION_NOTIFICATION
            putExtra(EXTRA_ACTION, action)
            notificationId?.let { putExtra(EXTRA_NOTIFICATION_ID, it) }
            putExtra(EXTRA_NOTIFY_ID, notifyId)
        }
    }
}
