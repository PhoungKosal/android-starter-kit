package com.t3r.android_starter_kit.data.remote.fcm


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
        // Handle data payload for in-app notifications
        // Override this to show local notifications or update UI state
    }
}
