package com.t3r.android_starter_kit.data.remote.fcm

import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.t3r.android_starter_kit.data.local.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Handles incoming FCM messages and token refresh.
 *
 * Data-only messages (backend `dataOnly=true`) use underscore-prefixed data keys:
 * `_title`, `_body`, `_channelId`, `_imageUrl`. Display messages use the standard
 * `notification` block. Both paths are handled in [onMessageReceived].
 */
@AndroidEntryPoint
class StarterKitMessagingService : FirebaseMessagingService() {

    @Inject lateinit var dataStoreManager: DataStoreManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNewToken(token: String) {
        Timber.d("FCM token refreshed")
        scope.launch {
            try {
                dataStoreManager.saveFcmToken(token)
                val isLoggedIn = dataStoreManager.isLoggedIn.first()
                DeviceTokenSyncWorker.enqueue(applicationContext, token, isLoggedIn)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save/enqueue refreshed FCM token")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            val data = message.data

            val title = message.notification?.title
                ?: data["_title"]
                ?: data["title"]
                ?: return Timber.w("FCM message dropped — no title")

            val body = message.notification?.body
                ?: data["_body"]
                ?: data["message"]
                ?: ""

            // Use backend notificationId as the Android notification ID when available.
            // This deduplicates: the same backend notification always maps to the same
            // Android ID, replacing rather than duplicating in the shade.
            val backendId = data["notificationId"]
            val notifyId = backendId?.hashCode()?.and(0x7FFFFFFF)
                ?: (idCounter.incrementAndGet() and 0x7FFFFFFF)

            val notification = NotificationBuilder.build(
                context = this,
                title = title,
                body = body,
                channelId = data["_channelId"],
                imageUrl = data["_imageUrl"],
                notificationId = backendId,
                actions = data["actions"],
                deepLink = data["deepLink"],
                notifyId = notifyId,
            )

            val manager = NotificationManagerCompat.from(this)
            manager.notify(notifyId, notification)
            manager.notify(
                NotificationChannels.SUMMARY_ID,
                NotificationBuilder.buildSummary(this, data["_channelId"]),
            )
        } catch (e: SecurityException) {
            Timber.w(e, "Notification permission not granted")
        } catch (e: Exception) {
            Timber.e(e, "Failed to process FCM message")
        }
    }

    private companion object {
        /** Monotonically increasing counter for unique notification IDs. */
        val idCounter = AtomicInteger(0)
    }
}
