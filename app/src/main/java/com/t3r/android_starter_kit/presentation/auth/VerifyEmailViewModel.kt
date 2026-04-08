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

data class VerifyEmailState(
    val isLoading: Boolean = false,
    val email: String = "",
    val message: String? = null,
    val error: AppError? = null,
)

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        VerifyEmailState(email = savedStateHandle.get<String>("email") ?: "")
    )
    val state: StateFlow<VerifyEmailState> = _state.asStateFlow()

    fun resend() {
        val email = _state.value.email
        if (email.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.resendVerification(email)
                .onSuccess { msg ->
                    _state.update { it.copy(isLoading = false, message = msg) }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
    fun clearError() = _state.update { it.copy(error = null) }
}
