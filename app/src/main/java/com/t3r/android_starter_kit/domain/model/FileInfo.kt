package com.t3r.android_starter_kit.domain.model

data class FileInfo(
    val id: String,
    val originalName: String,
    val mimeType: String,
    val size: Long,
    val url: String?,
    val createdAt: String?,
)

data class UploadUrl(
    val uploadToken: String,
    val uploadUrl: String,
    val key: String?,
    val expiresIn: Int,
)
