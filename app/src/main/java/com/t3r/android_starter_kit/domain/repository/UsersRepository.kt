package com.t3r.android_starter_kit.domain.repository

import com.starterkit.app.core.result.Result
import com.starterkit.app.domain.model.PaginatedData
import com.starterkit.app.domain.model.User

interface UsersRepository {

    suspend fun getUsers(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
    ): Result<PaginatedData<User>>

    suspend fun getUser(id: String): Result<User>

    suspend fun deleteUser(id: String): Result<Unit>
}
