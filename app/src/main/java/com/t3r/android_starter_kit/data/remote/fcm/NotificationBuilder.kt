package com.t3r.android_starter_kit.data.remote.fcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.t3r.android_starter_kit.MainActivity
import com.t3r.android_starter_kit.R
import timber.log.Timber
import java.net.URL

/**
 * Builds [android.app.Notification] instances from FCM payloads.
 *
 * Supports BigTextStyle, BigPictureStyle, action buttons via [NotificationActionReceiver],
 * notification grouping, and channel routing based on the backend `channelId`.
 */
object NotificationBuilder {

    fun build(
        context: Context,
        title: String,
        body: String,
        channelId: String?,
        imageUrl: String?,
        notificationId: String?,
        actions: String?,
        deepLink: String?,
        notifyId: Int,
    ): android.app.Notification {
        val resolvedChannel = NotificationChannels.resolve(channelId)
        val imageBitmap = imageUrl?.let { downloadBitmap(it) }

        // Tap intent — opens the app; extras carry deep link / notification ID
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deepLink?.let { putExtra(EXTRA_DEEP_LINK, it) }
            notificationId?.let { putExtra(EXTRA_NOTIFICATION_ID, it) }
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            notifyId,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val builder = NotificationCompat.Builder(context, resolvedChannel)
            .setSmallIcon(R.drawable.outline_code_xml_24)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .setGroup(NotificationChannels.GROUP_KEY)
            .setCategory(categoryForChannel(resolvedChannel))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        // Expanded style: image takes priority, otherwise big text for long bodies
        when {
            imageBitmap != null -> {
                builder.setLargeIcon(imageBitmap)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(imageBitmap)
                        .bigLargeIcon(null as Bitmap?),
                )
            }
            body.length > 40 -> {
                builder.setStyle(
                    NotificationCompat.BigTextStyle().bigText(body),
                )
            }
        }

        // Action buttons from backend (e.g. "mark_as_read,dismiss")
        actions?.split(",")?.map { it.trim() }?.forEach { action ->
            val actionIntent = NotificationActionReceiver.buildIntent(
                context = context,
                action = action,
                notificationId = notificationId,
                notifyId = notifyId,
            )
            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                "$notifyId-$action".hashCode(),
                actionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            builder.addAction(
                0,
                actionLabel(action),
                actionPendingIntent,
            )
        }

        return builder.build()
    }

    /**
     * Build a summary notification used to group individual notifications.
     * Shown when 2+ notifications of the same group are active.
     */
    fun buildSummary(context: Context, channelId: String?): android.app.Notification {
        val resolvedChannel = NotificationChannels.resolve(channelId)
        return NotificationCompat.Builder(context, resolvedChannel)
            .setSmallIcon(R.drawable.outline_code_xml_24)
            .setGroup(NotificationChannels.GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("New notifications"),
            )
            .build()
    }

    /** Map action identifiers to human-readable labels. */
    private fun actionLabel(action: String): String = when (action) {
        "mark_as_read" -> "Mark as Read"
        "mark_all_read" -> "Mark All Read"
        "dismiss" -> "Dismiss"
        else -> action.replace("_", " ")
            .replaceFirstChar { it.uppercaseChar() }
    }

    /** Map channel to a notification category for DND / priority handling. */
    private fun categoryForChannel(channelId: String): String = when (channelId) {
        NotificationChannels.SECURITY -> NotificationCompat.CATEGORY_STATUS
        NotificationChannels.SYSTEM -> NotificationCompat.CATEGORY_STATUS
        else -> NotificationCompat.CATEGORY_SOCIAL
    }

    /** Download a bitmap from a URL on the current thread (called from IO dispatcher). */
    private fun downloadBitmap(url: String): Bitmap? = try {
        val connection = URL(url).openConnection().apply {
            connectTimeout = 5_000
            readTimeout = 5_000
        }
        connection.getInputStream().use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        Timber.w(e, "Failed to download notification image: $url")
        null
    }

    internal const val EXTRA_DEEP_LINK = "extra_deep_link"
    internal const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
}
