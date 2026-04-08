package com.t3r.android_starter_kit.data.remote.fcm

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.t3r.android_starter_kit.MainActivity
import com.t3r.android_starter_kit.R
import com.t3r.android_starter_kit.StarterKitApp
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class StarterKitMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Timber.d("FCM token refreshed: $token")
        scope.launch {
            dataStoreManager.saveFcmToken(token)
            val isLoggedIn = dataStoreManager.isLoggedIn.first()
            if (isLoggedIn) {
                notificationsRepository.registerDevice(token, appVersion = null)
            } else {
                notificationsRepository.registerAnonymousDevice(token, appVersion = null)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("FCM message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["message"] ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(this, StarterKitApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Timber.w("Notification permission not granted")
        }
    }
}
