package com.t3r.android_starter_kit.domain.repository

import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.domain.model.FileInfo
import com.t3r.android_starter_kit.domain.model.UploadUrl

interface FilesRepository {

    suspend fun requestUploadUrl(
        originalName: String,
        mimeType: String,
        size: Long,
        visibility: String? = null,
    ): Result<UploadUrl>

    suspend fun confirmUpload(
        uploadToken: String,
        size: Long,
    ): Result<FileInfo>

    suspend fun uploadToPresignedUrl(
        url: String,
        bytes: ByteArray,
        mimeType: String,
    ): Result<Unit>

    suspend fun getFile(id: String): Result<FileInfo>

    suspend fun deleteFile(id: String): Result<Unit>

    suspend fun attachFile(
        fileId: String,
        entityType: String,
        entityId: String,
        purpose: String? = null,
    ): Result<Unit>

    suspend fun getEntityFiles(entityType: String, entityId: String): Result<List<FileInfo>>
}
