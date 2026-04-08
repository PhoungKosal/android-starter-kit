package com.t3r.android_starter_kit.data.remote.dto.files

import kotlinx.serialization.Serializable

@Serializable
data class RequestUploadUrlDto(
    val fileName: String,
    val mimeType: String,
    val size: Long,
)

@Serializable
data class UploadUrlResponseDto(
    val uploadToken: String,
    val uploadUrl: String,
    val expiresAt: String,
)

@Serializable
data class ConfirmUploadRequestDto(
    val uploadToken: String,
    val fileName: String? = null,
    val mimeType: String? = null,
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
