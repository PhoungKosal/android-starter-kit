package com.t3r.android_starter_kit.core.state

/**
 * A sealed interface representing the result of an operation.
 * Used across all layers for consistent error handling.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}

/**
 * Unified error model used throughout the app.
 * Maps directly from the backend's error response format.
 */
data class AppError(
    val code: String,
    val message: String,
    val status: Int = 0,
    val details: Map<String, Any>? = null,
)

/** Convenience extensions */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (AppError) -> Unit): Result<T> {
    if (this is Result.Error) action(error)
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data
