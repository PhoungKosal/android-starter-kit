package com.t3r.android_starter_kit

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.messaging.FirebaseMessaging
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.remote.fcm.DeviceTokenSyncWorker
import com.t3r.android_starter_kit.data.remote.fcm.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class StarterKitApp : Application(), Configuration.Provider {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        NotificationChannels.createAll(this)

        // Save the FCM token locally and enqueue a WorkManager job for reliable
        // backend registration (with retry). This is the single startup registration
        // path — onNewToken handles subsequent refreshes, and linkDeviceTokenToUser
        // in MainActivity handles the logged-out → logged-in transition.
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Timber.d("FCM token retrieved")
                appScope.launch {
                    dataStoreManager.saveFcmToken(token)
                    val isLoggedIn = dataStoreManager.isLoggedIn.first()
                    DeviceTokenSyncWorker.enqueue(this@StarterKitApp, token, isLoggedIn)
                }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Failed to retrieve FCM token")
            }
    }
}
