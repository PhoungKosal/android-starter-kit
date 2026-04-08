package com.t3r.android_starter_kit.data.repository

import com.t3r.android_starter_kit.core.network.safeApiCall
import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.core.result.map
import com.t3r.android_starter_kit.data.mapper.toDomain
import com.t3r.android_starter_kit.data.remote.api.FilesApi
import com.t3r.android_starter_kit.data.remote.dto.files.AttachFileRequestDto
import com.t3r.android_starter_kit.data.remote.dto.files.ConfirmUploadRequestDto
import com.t3r.android_starter_kit.data.remote.dto.files.RequestUploadUrlDto
import com.t3r.android_starter_kit.domain.model.FileInfo
import com.t3r.android_starter_kit.domain.model.UploadUrl
import com.t3r.android_starter_kit.domain.repository.FilesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class FilesRepositoryImpl @Inject constructor(
    private val filesApi: FilesApi,
    private val httpClient: OkHttpClient,
) : FilesRepository {

    override suspend fun requestUploadUrl(
        originalName: String,
        mimeType: String,
        size: Long,
        visibility: String?,
    ): Result<UploadUrl> = safeApiCall {
        filesApi.requestUploadUrl(RequestUploadUrlDto(originalName, mimeType, size, visibility))
    }.map { it.toDomain() }

    override suspend fun confirmUpload(
        uploadToken: String,
        size: Long,
    ): Result<FileInfo> = safeApiCall {
        filesApi.confirmUpload(ConfirmUploadRequestDto(uploadToken, size))
    }.map { it.toDomain() }

    override suspend fun uploadToPresignedUrl(
        url: String,
        bytes: ByteArray,
        mimeType: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = bytes.toRequestBody(mimeType.toMediaType())
            val request = Request.Builder().url(url).put(body).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(AppError("UPLOAD_FAILED", "Upload failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.Error(AppError("UPLOAD_FAILED", e.message ?: "Upload failed"))
        }
    }

    override suspend fun getFile(id: String): Result<FileInfo> = safeApiCall {
        filesApi.getFile(id)
    }.map { it.toDomain() }

    override suspend fun deleteFile(id: String): Result<Unit> = safeApiCall {
        filesApi.deleteFile(id)
    }

    override suspend fun attachFile(
        fileId: String,
        entityType: String,
        entityId: String,
        purpose: String?,
    ): Result<Unit> = safeApiCall {
        filesApi.attachFile(AttachFileRequestDto(fileId, entityType, entityId, purpose))
    }

    override suspend fun getEntityFiles(entityType: String, entityId: String): Result<List<FileInfo>> =
        safeApiCall {
            filesApi.getEntityFiles(entityType, entityId)
        }.map { list -> list.map { it.toDomain() } }
}
