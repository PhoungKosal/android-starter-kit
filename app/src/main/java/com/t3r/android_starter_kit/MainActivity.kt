package com.t3r.android_starter_kit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.data.remote.fcm.NotificationBuilder
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import com.t3r.android_starter_kit.navigation.AppNavigation
import com.t3r.android_starter_kit.presentation.theme.AndroidstarterkitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    private var isReady by mutableStateOf(false)
    private var isLoggedIn by mutableStateOf(false)

    /** Set when the app is opened via a notification tap; consumed by [AppNavigation]. */
    var pendingNotificationRoute by mutableStateOf(false)
        private set

    /** Deep link extracted from the notification intent, if any. */
    var pendingDeepLink by mutableStateOf<String?>(null)
        private set

    fun consumeNotificationRoute() {
        pendingNotificationRoute = false
        pendingDeepLink = null
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        Timber.d("POST_NOTIFICATIONS permission granted: $granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash screen until we determine auth state
        splashScreen.setKeepOnScreenCondition { !isReady }

        // Restore pending notification state across configuration changes
        if (savedInstanceState != null) {
            pendingNotificationRoute = savedInstanceState.getBoolean(KEY_PENDING_NOTIFICATION, false)
            pendingDeepLink = savedInstanceState.getString(KEY_PENDING_DEEP_LINK)
        } else {
            handleNotificationIntent(intent)
        }

        // Observe auth state reactively so the UI updates when session expires
        lifecycleScope.launch {
            var wasLoggedIn = false
            dataStoreManager.isLoggedIn.collect { loggedIn ->
                isLoggedIn = loggedIn
                if (!isReady) isReady = true

                // When user transitions from logged-out → logged-in, ensure
                // the device token is linked to their account. This acts as a
                // safety net in case registerFcmDevice() in the login flow
                // failed (e.g. FCM token not yet available).
                if (loggedIn && !wasLoggedIn) {
                    linkDeviceTokenToUser()
                }
                wasLoggedIn = loggedIn
            }
        }

        requestNotificationPermission()
        enableEdgeToEdge()

        setContent {
            AndroidstarterkitTheme {
                if (isReady) {
                    AppNavigation(
                        isLoggedIn = isLoggedIn,
                        openNotifications = pendingNotificationRoute,
                        pendingDeepLink = pendingDeepLink,
                        onNotificationsOpened = ::consumeNotificationRoute,
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_PENDING_NOTIFICATION, pendingNotificationRoute)
        outState.putString(KEY_PENDING_DEEP_LINK, pendingDeepLink)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    /** Check if the intent was launched from a notification tap. */
    private fun handleNotificationIntent(intent: Intent?) {
        val notificationId = intent?.getStringExtra(NotificationBuilder.EXTRA_NOTIFICATION_ID)
        val deepLink = intent?.getStringExtra(NotificationBuilder.EXTRA_DEEP_LINK)
        if (notificationId != null || deepLink != null) {
            pendingNotificationRoute = true
            pendingDeepLink = deepLink
            // Clear extras so the same intent doesn't re-trigger on config change
            intent.removeExtra(NotificationBuilder.EXTRA_NOTIFICATION_ID)
            intent.removeExtra(NotificationBuilder.EXTRA_DEEP_LINK)
        }
    }

    /** Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33). */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) return
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * Link the device's FCM token to the authenticated user.
     * Called when isLoggedIn flips to true — covers normal login, 2FA login,
     * and any edge case where the login-time registration was missed.
     */
    private fun linkDeviceTokenToUser() {
        lifecycleScope.launch {
            try {
                val fcmToken = dataStoreManager.fcmToken.first()
                    ?: suspendCancellableCoroutine { cont ->
                        FirebaseMessaging.getInstance().token
                            .addOnSuccessListener { token ->
                                lifecycleScope.launch { dataStoreManager.saveFcmToken(token) }
                                cont.resume(token)
                            }
                            .addOnFailureListener { cont.resumeWithException(it) }
                    }
                notificationsRepository.registerDevice(fcmToken, appVersion = null)
                Timber.d("Device token linked to user after login")
            } catch (e: Exception) {
                Timber.e(e, "Failed to link device token after login")
            }
        }
    }

    private companion object {
        const val KEY_PENDING_NOTIFICATION = "pending_notification_route"
        const val KEY_PENDING_DEEP_LINK = "pending_deep_link"
    }
}
