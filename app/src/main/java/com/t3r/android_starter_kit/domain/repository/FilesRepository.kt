package com.t3r.android_starter_kit.domain.repository

import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.domain.model.FileInfo
import com.t3r.android_starter_kit.domain.model.UploadUrl

interface FilesRepository {

    suspend fun requestUploadUrl(
        fileName: String,
        mimeType: String,
        size: Long,
    ): Result<UploadUrl>

    suspend fun confirmUpload(
        uploadToken: String,
        fileName: String? = null,
        mimeType: String? = null,
    ): Result<FileInfo>

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
