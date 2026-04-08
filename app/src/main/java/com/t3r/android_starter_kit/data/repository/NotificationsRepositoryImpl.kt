package com.t3r.android_starter_kit.data.repository

import com.starterkit.app.core.network.safeApiCall
import com.starterkit.app.core.result.Result
import com.starterkit.app.core.result.map
import com.starterkit.app.data.mapper.toDomain
import com.starterkit.app.data.remote.api.NotificationsApi
import com.starterkit.app.data.remote.dto.notifications.RegisterDeviceRequestDto
import com.starterkit.app.domain.model.Notification
import com.starterkit.app.domain.model.PaginatedData
import com.starterkit.app.domain.repository.NotificationsRepository
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val notificationsApi: NotificationsApi,
) : NotificationsRepository {

    override suspend fun getNotifications(page: Int, limit: Int): Result<PaginatedData<Notification>> =
        safeApiCall {
            notificationsApi.getNotifications(page, limit)
        }.map { response ->
            PaginatedData(
                data = response.data.map { it.toDomain() },
                currentPage = response.meta.currentPage,
                totalPages = response.meta.totalPages,
                totalItems = response.meta.totalItems,
                itemsPerPage = response.meta.itemsPerPage,
            )
        }

    override suspend fun getUnreadNotifications(): Result<List<Notification>> = safeApiCall {
        notificationsApi.getUnreadNotifications()
    }.map { list -> list.map { it.toDomain() } }

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
