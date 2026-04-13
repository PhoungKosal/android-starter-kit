package com.t3r.android_starter_kit.core.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShieldMoon
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.t3r.android_starter_kit.R
import com.t3r.android_starter_kit.domain.model.Notification
import com.t3r.android_starter_kit.domain.model.NotificationType

/**
 * Client-side notification configuration.
 *
 * Maps notification keys to their display label, icon, and default link.
 * Mirrors the frontend `notification.config.ts` and `useNotifications.getNotificationLabel()`.
 *
 * When the backend sends a notification with a `key` (e.g. "twoFA.enabled"),
 * the display label is resolved from string resources (localized), and the icon
 * is resolved from this config. If no `key` is present, falls back to
 * `notification.title` / `notification.message`.
 *
 * Keep in sync with:
 *   - backend: src/modules/notifications/constants/notification-keys.const.ts
 *   - frontend: src/config/notification.config.ts
 */
object NotificationConfig {

    private data class Config(
        val labelRes: Int,
        val icon: ImageVector,
        val link: String? = null,
    )

    private val keyConfigs = mapOf(
        "twoFA.enabled" to Config(
            labelRes = R.string.notification_twofa_enabled,
            icon = Icons.Outlined.ShieldMoon,
            link = "/settings/security",
        ),
        "twoFA.disabled" to Config(
            labelRes = R.string.notification_twofa_disabled,
            icon = Icons.Outlined.ShieldMoon,
            link = "/settings/security",
        ),
        "account.activated" to Config(
            labelRes = R.string.notification_account_activated,
            icon = Icons.Outlined.Person,
            link = "/settings",
        ),
        "security.alert" to Config(
            labelRes = R.string.notification_security_alert,
            icon = Icons.Outlined.Warning,
            link = "/settings/security",
        ),
        "password.changed" to Config(
            labelRes = R.string.notification_password_changed,
            icon = Icons.Outlined.Lock,
            link = "/settings/security",
        ),
    )

    /**
     * Resolve the display label for a notification.
     * If the notification has a `key`, returns the localized string.
     * Otherwise falls back to `title` → `message` → empty string.
     */
    @Composable
    fun getLabel(notification: Notification): String {
        val key = notification.key
        if (key != null) {
            val config = keyConfigs[key]
            if (config != null) return stringResource(config.labelRes)
            // Unknown key — return the raw key as fallback
            return key
        }
        return notification.title ?: notification.message ?: ""
    }

    /**
     * Resolve the icon for a notification based on its key, then type.
     */
    fun getIcon(notification: Notification): ImageVector {
        val key = notification.key
        if (key != null) {
            val config = keyConfigs[key]
            if (config != null) return config.icon
        }
        return typeIcon(notification.type)
    }

    private fun typeIcon(type: NotificationType): ImageVector = when (type) {
        NotificationType.SUCCESS -> Icons.Outlined.CheckCircle
        NotificationType.WARNING -> Icons.Outlined.Warning
        NotificationType.ERROR -> Icons.Outlined.Warning
        NotificationType.INFO -> Icons.Outlined.Info
        NotificationType.GENERAL -> Icons.Outlined.Notifications
    }
}
