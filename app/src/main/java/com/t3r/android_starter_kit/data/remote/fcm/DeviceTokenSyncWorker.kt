package com.t3r.android_starter_kit.data.remote.fcm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.dto.notifications.RegisterDeviceRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Reliably syncs the FCM device token with the backend.
 * Retries automatically on network failure and deduplicates via unique work.
 */
@HiltWorker
class DeviceTokenSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationsApi: NotificationsApi,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val token = inputData.getString(KEY_TOKEN) ?: return Result.failure()
        val isLoggedIn = inputData.getBoolean(KEY_IS_LOGGED_IN, false)

        return try {
            val request = RegisterDeviceRequestDto(
                token = token,
                platform = "android",
                appVersion = null,
            )
            if (isLoggedIn) {
                notificationsApi.registerDevice(request)
            } else {
                notificationsApi.registerAnonymousDevice(request)
            }
            Timber.d("Device token synced with backend (loggedIn=$isLoggedIn)")
            Result.success()
        } catch (e: Exception) {
            Timber.w(e, "Failed to sync device token, will retry")
            Result.retry()
        }
    }

    companion object {
        private const val KEY_TOKEN = "fcm_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val UNIQUE_WORK_NAME = "device_token_sync"

        fun enqueue(context: Context, token: String, isLoggedIn: Boolean) {
            val request = OneTimeWorkRequestBuilder<DeviceTokenSyncWorker>()
                .setInputData(
                    workDataOf(
                        KEY_TOKEN to token,
                        KEY_IS_LOGGED_IN to isLoggedIn,
                    ),
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
