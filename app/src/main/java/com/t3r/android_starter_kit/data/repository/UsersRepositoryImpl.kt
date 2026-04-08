package com.t3r.android_starter_kit.data.repository

import com.t3r.android_starter_kit.core.network.safeApiCall
import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.core.result.map
import com.t3r.android_starter_kit.data.mapper.toDomain
import com.t3r.android_starter_kit.data.remote.api.UsersApi
import com.t3r.android_starter_kit.domain.model.PaginatedData
import com.t3r.android_starter_kit.domain.model.User
import com.t3r.android_starter_kit.domain.repository.UsersRepository
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
) : UsersRepository {

    override suspend fun getUsers(
        page: Int,
        limit: Int,
        search: String?,
    ): Result<PaginatedData<User>> = safeApiCall {
        usersApi.getUsers(page, limit, search)
    }.map { response ->
        PaginatedData(
            data = response.data.map { it.toDomain() },
            currentPage = response.meta.currentPage,
            totalPages = response.meta.totalPages,
            totalItems = response.meta.totalItems,
            itemsPerPage = response.meta.itemsPerPage,
        )
    }

    override suspend fun getUser(id: String): Result<User> = safeApiCall {
        usersApi.getUser(id)
    }.map { it.toDomain() }

    override suspend fun deleteUser(id: String): Result<Unit> = safeApiCall {
        usersApi.deleteUser(id)
    }
}
