package com.t3r.android_starter_kit.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3r.android_starter_kit.core.result.AppError
import com.t3r.android_starter_kit.core.result.onError
import com.t3r.android_starter_kit.core.result.onSuccess
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordState(
    val token: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: AppError? = null,
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ResetPasswordState(token = savedStateHandle.get<String>("token") ?: "")
    )
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    fun updatePassword(value: String) = _state.update { it.copy(password = value) }
    fun updateConfirmPassword(value: String) = _state.update { it.copy(confirmPassword = value) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun resetPassword() {
        val s = _state.value
        if (s.password.isBlank() || s.password != s.confirmPassword) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.resetPassword(s.token, s.password)
                .onSuccess { _state.update { it.copy(isLoading = false, success = true) } }
                .onError { error -> _state.update { it.copy(isLoading = false, error = error) } }
        }
    }
}
