package com.t3r.android_starter_kit.data.remote.dto.common

import kotlinx.serialization.Serializable

/** Maps to the backend's unified error response format. */
@Serializable
data class ErrorResponseDto(
    val code: String,
    val message: String,
    val status: Int = 0,
    val details: Map<String, String>? = null,
)

/** Generic paginated response wrapper. */
@Serializable
data class PaginatedResponseDto<T>(
    val data: List<T>,
    val meta: PaginationMetaDto,
)

@Serializable
data class PaginationMetaDto(
    val itemsPerPage: Int,
    val totalItems: Int,
    val currentPage: Int,
    val totalPages: Int,
)

/** Simple message response. */
@Serializable
data class MessageResponseDto(
    val message: String,
)
