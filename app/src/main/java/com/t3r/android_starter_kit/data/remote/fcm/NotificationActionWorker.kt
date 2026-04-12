package com.t3r.android_starter_kit.data.remote.fcm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.dto.notifications.NotificationActionRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Reliably delivers notification actions (mark_as_read, dismiss, etc.) to the
 * backend. Uses WorkManager so the request survives process death and retries
 * automatically on network failure.
 */
@HiltWorker
class NotificationActionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationsApi: NotificationsApi,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationId = inputData.getString(KEY_NOTIFICATION_ID)
        val action = inputData.getString(KEY_ACTION) ?: return Result.failure()

        return try {
            notificationsApi.handleAction(
                NotificationActionRequestDto(
                    notificationId = notificationId,
                    action = action,
                ),
            )
            Timber.d("Action '$action' sent to backend successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.w(e, "Failed to send action '$action', will retry")
            Result.retry()
        }
    }

    companion object {
        private const val KEY_NOTIFICATION_ID = "notification_id"
        private const val KEY_ACTION = "action"

        fun enqueue(context: Context, notificationId: String?, action: String) {
            val request = OneTimeWorkRequestBuilder<NotificationActionWorker>()
                .setInputData(
                    workDataOf(
                        KEY_NOTIFICATION_ID to notificationId,
                        KEY_ACTION to action,
                    ),
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
