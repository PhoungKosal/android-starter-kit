package com.t3r.android_starter_kit.data.repository

import com.t3r.android_starter_kit.core.network.safeApiCall
import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.core.result.map
import com.t3r.android_starter_kit.data.mapper.toDomain
import com.t3r.android_starter_kit.data.remote.api.NotificationsApi
import com.t3r.android_starter_kit.data.remote.dto.notifications.RegisterDeviceRequestDto
import com.t3r.android_starter_kit.domain.model.Notification
import com.t3r.android_starter_kit.domain.model.PaginatedData
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val notificationsApi: NotificationsApi,
) : NotificationsRepository {

    override suspend fun getNotifications(page: Int, limit: Int): Result<PaginatedData<Notification>> =
        safeApiCall {
            val offset = (page - 1) * limit
            notificationsApi.getNotifications(offset, limit)
        }.map { list ->
            PaginatedData(
                data = list.map { it.toDomain() },
                currentPage = page,
                totalPages = if (list.size >= limit) page + 1 else page,
                totalItems = -1, // Unknown without backend total count header
                itemsPerPage = limit,
            )
        }

    override suspend fun getUnreadCount(): Result<Int> = safeApiCall {
        notificationsApi.getUnreadCount()
    }.map { it.count }

    override suspend fun markAsRead(id: String): Result<Notification> = safeApiCall {
        notificationsApi.markAsRead(id)
    }.map { it.toDomain() }

    override suspend fun markAllAsRead(): Result<Unit> = safeApiCall {
        notificationsApi.markAllAsRead()
    }

    override suspend fun deleteNotification(id: String): Result<Unit> = safeApiCall {
        notificationsApi.deleteNotification(id)
    }

    override suspend fun registerDevice(token: String, appVersion: String?): Result<Unit> =
        safeApiCall {
            notificationsApi.registerDevice(
                RegisterDeviceRequestDto(token = token, platform = "android", appVersion = appVersion)
            )
        }

    override suspend fun registerAnonymousDevice(token: String, appVersion: String?): Result<Unit> =
        safeApiCall {
            notificationsApi.registerAnonymousDevice(
                RegisterDeviceRequestDto(token = token, platform = "android", appVersion = appVersion)
            )
        }

    override suspend fun unregisterDevice(token: String): Result<Unit> = safeApiCall {
        notificationsApi.unregisterDevice(token)
    }
}
