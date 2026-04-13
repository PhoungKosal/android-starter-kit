package com.t3r.android_starter_kit.core.network

import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.core.result.Result
import com.t3r.android_starter_kit.data.remote.dto.common.ErrorResponseDto
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * Safely executes a suspend API call and wraps the result.
 * Handles HTTP errors by parsing the backend error format,
 * and network errors with a generic message.
 *
 * IMPORTANT: [CancellationException] is always re-thrown to preserve
 * Kotlin structured concurrency. Swallowing it would prevent coroutine
 * cancellation from propagating correctly (e.g. when a ViewModel is cleared).
 */
suspend fun <T> safeApiCall(
    json: Json = Json { ignoreUnknownKeys = true },
    apiCall: suspend () -> T,
): Result<T> {
    return try {
        Result.Success(apiCall())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val errorDto = errorBody?.let {
            try {
                json.decodeFromString<ErrorResponseDto>(it)
            } catch (_: Exception) {
                null
            }
        }
        Result.Error(
            AppError(
                code = errorDto?.code ?: "HTTP_${e.code()}",
                message = errorDto?.message ?: e.message(),
                status = e.code(),
                details = errorDto?.details,
            )
        )
    } catch (_: IOException) {
        Result.Error(
            AppError(
                code = "NETWORK_ERROR",
                message = "Unable to connect. Please check your internet connection.",
            )
        )
    } catch (e: Exception) {
        Result.Error(
            AppError(
                code = "UNKNOWN_ERROR",
                message = e.message ?: "An unexpected error occurred.",
            )
        )
    }
}
