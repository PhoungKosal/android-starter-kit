package com.t3r.android_starter_kit.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starterkit.app.core.result.onError
import com.starterkit.app.core.result.onSuccess
import com.starterkit.app.domain.model.LoginResult
import com.starterkit.app.domain.model.RegisterResult
import com.starterkit.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthNavigationEvent {
    data object NavigateToHome : AuthNavigationEvent
    data object NavigateToTwoFactor : AuthNavigationEvent
    data object NavigateToVerifyEmail : AuthNavigationEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _navigation = MutableSharedFlow<AuthNavigationEvent>()
    val navigation = _navigation.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            // Login field updates
            is AuthEvent.UpdateIdentifier -> _state.update { it.copy(identifier = event.value) }
            is AuthEvent.UpdatePassword -> _state.update { it.copy(password = event.value) }
            is AuthEvent.TogglePasswordVisibility -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }

            // Register field updates
            is AuthEvent.UpdateEmail -> _state.update { it.copy(email = event.value) }
            is AuthEvent.UpdateUsername -> _state.update { it.copy(username = event.value) }
            is AuthEvent.UpdateRegisterPassword -> _state.update { it.copy(registerPassword = event.value) }
            is AuthEvent.UpdateFirstName -> _state.update { it.copy(firstName = event.value) }
            is AuthEvent.UpdateLastName -> _state.update { it.copy(lastName = event.value) }

            // 2FA
            is AuthEvent.UpdateTwoFactorCode -> _state.update { it.copy(twoFactorCode = event.value) }

            // Forgot password
            is AuthEvent.UpdateForgotPasswordEmail -> _state.update { it.copy(forgotPasswordEmail = event.value) }

            // Actions
            is AuthEvent.Login -> login()
            is AuthEvent.Register -> register()
            is AuthEvent.VerifyTwoFactor -> verifyTwoFactor()
            is AuthEvent.SendForgotPassword -> sendForgotPassword()

            // Clear
            is AuthEvent.ClearError -> _state.update { it.copy(error = null) }
            is AuthEvent.ClearSuccessMessage -> _state.update { it.copy(successMessage = null) }
        }
    }

    private fun login() {
        val currentState = _state.value
        if (currentState.identifier.isBlank() || currentState.password.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository
                .login(currentState.identifier, currentState.password)
                .onSuccess { result ->
                    _state.update { it.copy(isLoading = false) }
                    when (result) {
                        is LoginResult.Authenticated -> {
                            _navigation.emit(AuthNavigationEvent.NavigateToHome)
                        }
                        is LoginResult.TwoFactorRequired -> {
                            _state.update {
                                it.copy(
                                    requiresTwoFactor = true,
                                    challengeToken = result.challengeToken,
                                )
                            }
                            _navigation.emit(AuthNavigationEvent.NavigateToTwoFactor)
                        }
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    private fun register() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.username.isBlank() || currentState.registerPassword.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository
                .register(
                    email = currentState.email,
                    username = currentState.username,
                    password = currentState.registerPassword,
                    firstName = currentState.firstName.ifBlank { null },
                    lastName = currentState.lastName.ifBlank { null },
                )
                .onSuccess { result ->
                    _state.update { it.copy(isLoading = false) }
                    when (result) {
                        is RegisterResult.Authenticated -> {
                            _navigation.emit(AuthNavigationEvent.NavigateToHome)
                        }
                        is RegisterResult.VerificationRequired -> {
                            _state.update { it.copy(successMessage = result.message) }
                            _navigation.emit(AuthNavigationEvent.NavigateToVerifyEmail)
                        }
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    private fun verifyTwoFactor() {
        val currentState = _state.value
        if (currentState.twoFactorCode.isBlank() || currentState.challengeToken.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository
                .verify2fa(currentState.challengeToken, currentState.twoFactorCode)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _navigation.emit(AuthNavigationEvent.NavigateToHome)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    private fun sendForgotPassword() {
        val currentState = _state.value
        if (currentState.forgotPasswordEmail.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository
                .forgotPassword(currentState.forgotPasswordEmail)
                .onSuccess { message ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            forgotPasswordSent = true,
                            successMessage = message,
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }
}
