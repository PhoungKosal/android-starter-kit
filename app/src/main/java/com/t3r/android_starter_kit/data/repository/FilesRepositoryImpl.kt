package com.t3r.android_starter_kit.data.repository


import com.t3r.android_starter_kit.core.network.safeApiCall
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
import javax.inject.Inject

class FilesRepositoryImpl @Inject constructor(
    private val filesApi: FilesApi,
) : FilesRepository {

    override suspend fun requestUploadUrl(
        fileName: String,
        mimeType: String,
        size: Long,
    ): Result<UploadUrl> = safeApiCall {
        filesApi.requestUploadUrl(RequestUploadUrlDto(fileName, mimeType, size))
    }.map { it.toDomain() }

    override suspend fun confirmUpload(
        uploadToken: String,
        fileName: String?,
        mimeType: String?,
    ): Result<FileInfo> = safeApiCall {
        filesApi.confirmUpload(ConfirmUploadRequestDto(uploadToken, fileName, mimeType))
    }.map { it.toDomain() }

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
