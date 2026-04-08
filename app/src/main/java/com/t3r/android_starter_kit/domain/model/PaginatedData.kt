package com.t3r.android_starter_kit.domain.model

data class PaginatedData<T>(
    val data: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int,
) {
    val hasMore: Boolean get() = currentPage < totalPages
}
