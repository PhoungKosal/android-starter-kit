package com.t3r.android_starter_kit.data.remote.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber

/**
 * Handles notification action button taps (e.g. "Mark as Read", "Dismiss").
 *
 * Cancels the notification from the shade and enqueues the action
 * via [NotificationActionWorker] for reliable delivery to the backend.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return
        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val notifyId = intent.getIntExtra(EXTRA_NOTIFY_ID, -1)

        Timber.d("Notification action received: action=$action, notificationId=$notificationId")

        // Dismiss the notification from the shade
        try {
            NotificationManagerCompat.from(context).cancel(notifyId)
        } catch (_: SecurityException) {
            Timber.w("Cannot cancel notification — permission not granted")
        }

        // Reliable delivery via WorkManager (survives process death, retries on failure)
        NotificationActionWorker.enqueue(context, notificationId, action)
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
