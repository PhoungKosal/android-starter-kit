package com.t3r.android_starter_kit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.messaging.FirebaseMessaging
import com.t3r.android_starter_kit.data.local.DataStoreManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class StarterKitApp : Application() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createNotificationChannel()

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Timber.d("FCM Token: $token")
            appScope.launch {
                dataStoreManager.saveFcmToken(token)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "General Notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "App notifications"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "starterkit_notifications"
    }
}
