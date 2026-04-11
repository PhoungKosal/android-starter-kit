package com.t3r.android_starter_kit

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.remote.fcm.NotificationChannels
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class StarterKitApp : Application() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        NotificationChannels.createAll(this)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Timber.d("FCM Token: $token")
                appScope.launch {
                    dataStoreManager.saveFcmToken(token)
                    try {
                        val isLoggedIn = dataStoreManager.isLoggedIn.first()
                        if (isLoggedIn) {
                            notificationsRepository.registerDevice(token, appVersion = null)
                        } else {
                            notificationsRepository.registerAnonymousDevice(token, appVersion = null)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to register FCM token on startup")
                    }
                }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Failed to retrieve FCM token")
            }
    }
}
