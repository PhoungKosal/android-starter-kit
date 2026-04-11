package com.t3r.android_starter_kit

import android.Manifest
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
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.navigation.AppNavigation
import com.t3r.android_starter_kit.presentation.theme.AndroidstarterkitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private var isReady by mutableStateOf(false)
    private var isLoggedIn by mutableStateOf(false)

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

        // Observe auth state reactively so the UI updates when session expires
        lifecycleScope.launch {
            dataStoreManager.isLoggedIn.collect { loggedIn ->
                isLoggedIn = loggedIn
                if (!isReady) isReady = true
            }
        }

        requestNotificationPermission()
        enableEdgeToEdge()

        setContent {
            AndroidstarterkitTheme {
                if (isReady) {
                    AppNavigation(isLoggedIn = isLoggedIn)
                }
            }
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
}
