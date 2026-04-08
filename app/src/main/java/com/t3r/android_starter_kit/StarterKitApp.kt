package com.t3r.android_starter_kit

import android.app.Application
import com.t3r.android_starter_kit.BuildConfig
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

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Capture initial FCM token
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Timber.d("FCM Token: $token")
            appScope.launch {
                dataStoreManager.saveFcmToken(token)
            }
        }
    }
}
