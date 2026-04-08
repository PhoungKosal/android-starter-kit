package com.t3r.android_starter_kit.domain.repository

import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.domain.model.Notification
import com.t3r.android_starter_kit.domain.model.PaginatedData

interface NotificationsRepository {

    suspend fun getNotifications(page: Int = 1, limit: Int = 20): Result<PaginatedData<Notification>>

    suspend fun getUnreadNotifications(): Result<List<Notification>>

    suspend fun getUnreadCount(): Result<Int>

    suspend fun markAsRead(id: String): Result<Notification>

    suspend fun markAllAsRead(): Result<Unit>

    suspend fun deleteNotification(id: String): Result<Unit>

    suspend fun registerDevice(token: String, appVersion: String?): Result<Unit>

    suspend fun registerAnonymousDevice(token: String, appVersion: String?): Result<Unit>

    suspend fun unregisterDevice(token: String): Result<Unit>
}
