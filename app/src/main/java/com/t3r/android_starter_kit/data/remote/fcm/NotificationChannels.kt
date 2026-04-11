package com.t3r.android_starter_kit.data.remote.fcm

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context

/**
 * Notification channel definitions.
 *
 * Channel IDs must match the backend `channelId` values sent in FCM payloads.
 */
object NotificationChannels {

    const val GENERAL = "general"
    const val SYSTEM = "system"
    const val SECURITY = "security"

    // Group ID
    private const val GROUP_APP = "app_notifications"

    // Summary notification group key
    const val GROUP_KEY = "com.t3r.android_starter_kit.NOTIFICATIONS"

    // Summary notification ID (stable so it gets replaced, not stacked)
    const val SUMMARY_ID = 0

    fun createAll(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Create group
        manager.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_APP, "App Notifications"),
        )

        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "General app notifications"
                    group = GROUP_APP
                },
                NotificationChannel(
                    SYSTEM,
                    "System",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Account and system updates"
                    group = GROUP_APP
                },
                NotificationChannel(
                    SECURITY,
                    "Security",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Security alerts and password changes"
                    group = GROUP_APP
                },
            ),
        )
    }

    /** Resolve a backend channelId to a valid local channel, falling back to [GENERAL]. */
    fun resolve(channelId: String?): String = when (channelId) {
        SYSTEM -> SYSTEM
        SECURITY -> SECURITY
        else -> GENERAL
    }
}
