package com.t3r.android_starter_kit.data.remote.fcm


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


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
