package com.t3r.android_starter_kit.data.remote.dto.files

import kotlinx.serialization.Serializable

@Serializable
data class RequestUploadUrlDto(
    val originalName: String,
    val mimeType: String,
    val size: Long,
    val visibility: String? = null,
)

@Serializable
data class UploadUrlResponseDto(
    val uploadToken: String,
    val uploadUrl: String,
    val key: String? = null,
    val expiresIn: Int = 0,
)

@Serializable
data class ConfirmUploadRequestDto(
    val uploadToken: String,
    val size: Long,
)

@Serializable
data class FileDto(
    val id: String,
    val originalName: String,
    val mimeType: String,
    val size: Long,
    val url: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class AttachFileRequestDto(
    val fileId: String,
    val entityType: String,
    val entityId: String,
    val purpose: String? = null,
)
