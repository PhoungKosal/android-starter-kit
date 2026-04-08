package com.t3r.android_starter_kit.core.state

import com.t3r.android_starter_kit.core.result.AppError

/**
 * Generic UI state holder for screens.
 * Represents the three primary states any screen can be in.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}
