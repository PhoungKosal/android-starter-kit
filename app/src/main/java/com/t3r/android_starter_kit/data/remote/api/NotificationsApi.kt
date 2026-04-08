package com.t3r.android_starter_kit.data.remote.api

interface NotificationsApi {

    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): PaginatedResponseDto<NotificationDto>

    @GET("notifications/unread")
    suspend fun getUnreadNotifications(): List<NotificationDto>

    @GET("notifications/unread/count")
    suspend fun getUnreadCount(): UnreadCountDto

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): NotificationDto

    @POST("notifications/read-all")
    suspend fun markAllAsRead()

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String)

    @POST("notifications/action")
    suspend fun handleAction(@Body request: NotificationActionRequestDto)

    @POST("notifications/devices")
    suspend fun registerDevice(@Body request: RegisterDeviceRequestDto)

    @POST("notifications/devices/anonymous")
    suspend fun registerAnonymousDevice(@Body request: RegisterDeviceRequestDto)

    @DELETE("notifications/devices/{token}")
    suspend fun unregisterDevice(@Path("token") token: String)
}