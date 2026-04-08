package com.t3r.android_starter_kit.data.remote.api

import com.t3r.android_starter_kit.data.remote.dto.common.PaginatedResponseDto
import com.t3r.android_starter_kit.data.remote.dto.files.AttachFileRequestDto
import com.t3r.android_starter_kit.data.remote.dto.files.ConfirmUploadRequestDto
import com.t3r.android_starter_kit.data.remote.dto.files.FileDto
import com.t3r.android_starter_kit.data.remote.dto.files.RequestUploadUrlDto
import com.t3r.android_starter_kit.data.remote.dto.files.UploadUrlResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FilesApi {

    @POST("files/upload-url")
    suspend fun requestUploadUrl(@Body request: RequestUploadUrlDto): UploadUrlResponseDto

    @POST("files/confirm-upload")
    suspend fun confirmUpload(@Body request: ConfirmUploadRequestDto): FileDto

    @GET("files")
    suspend fun getFiles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): PaginatedResponseDto<FileDto>

    @GET("files/{id}")
    suspend fun getFile(@Path("id") id: String): FileDto

    @DELETE("files/{id}")
    suspend fun deleteFile(@Path("id") id: String)

    @POST("files/attach")
    suspend fun attachFile(@Body request: AttachFileRequestDto)

    @GET("files/entity/{entityType}/{entityId}")
    suspend fun getEntityFiles(
        @Path("entityType") entityType: String,
        @Path("entityId") entityId: String,
    ): List<FileDto>
}