package com.t3r.android_starter_kit.data.remote.fcm

import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
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
    @Inject lateinit var notificationsRepository: NotificationsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Timber.d("FCM token refreshed")
        scope.launch {
            try {
                dataStoreManager.saveFcmToken(token)
                if (dataStoreManager.isLoggedIn.first()) {
                    notificationsRepository.registerDevice(token, appVersion = null)
                } else {
                    notificationsRepository.registerAnonymousDevice(token, appVersion = null)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to register refreshed FCM token")
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

            val notifyId = System.currentTimeMillis().toInt()

            val notification = NotificationBuilder.build(
                context = this,
                title = title,
                body = body,
                channelId = data["_channelId"],
                imageUrl = data["_imageUrl"],
                notificationId = data["notificationId"],
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
}
