package com.t3r.android_starter_kit.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class TwoFactorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TwoFactorState())
    val state: StateFlow<TwoFactorState> = _state.asStateFlow()

    fun setInitialEnabled(enabled: Boolean) {
        _state.update { it.copy(twoFactorEnabled = enabled) }
    }

    fun onEvent(event: TwoFactorEvent) {
        when (event) {
            TwoFactorEvent.RequestEnable -> requestEnable()
            is TwoFactorEvent.UpdateVerifyCode -> _state.update { it.copy(verifyCode = event.value) }
            TwoFactorEvent.ConfirmEnable -> confirmEnable()
            TwoFactorEvent.DismissSetupDialog -> _state.update { it.copy(showSetupDialog = false) }
            TwoFactorEvent.ShowDisableDialog -> _state.update { it.copy(showDisableDialog = true) }
            TwoFactorEvent.DismissDisableDialog -> _state.update { it.copy(showDisableDialog = false, disablePassword = "") }
            is TwoFactorEvent.UpdateDisablePassword -> _state.update { it.copy(disablePassword = event.value) }
            TwoFactorEvent.ConfirmDisable -> confirmDisable()
            TwoFactorEvent.ClearMessage -> _state.update { it.copy(message = null, error = null) }
        }
    }

    private fun requestEnable() {
        _state.update { it.copy(showSetupDialog = true, verifyCode = "") }
    }

    private fun confirmEnable() {
        val code = _state.value.verifyCode
        if (code.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.enable2fa(code)
                .onSuccess { setup ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            twoFactorEnabled = true,
                            qrCodeUrl = setup.qrCodeUrl,
                            secret = setup.secret,
                            recoveryCodes = setup.recoveryCodes,
                            message = "Two-factor authentication enabled",
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    private fun confirmDisable() {
        val password = _state.value.disablePassword
        if (password.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.disable2fa(password)
                .onSuccess { msg ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            twoFactorEnabled = false,
                            showDisableDialog = false,
                            disablePassword = "",
                            message = msg,
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }
}
