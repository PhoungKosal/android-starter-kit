package com.t3r.android_starter_kit.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3r.android_starter_kit.core.result.onError
import com.t3r.android_starter_kit.core.result.onSuccess
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileNavigationEvent {
    data object AccountDeleted : ProfileNavigationEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _navigation = MutableSharedFlow<ProfileNavigationEvent>()
    val navigation = _navigation.asSharedFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadProfile -> loadProfile()
            ProfileEvent.Logout -> logout()
            ProfileEvent.StartEditing -> startEditing()
            ProfileEvent.CancelEditing -> _state.update { it.copy(isEditing = false) }
            is ProfileEvent.UpdateFirstName -> _state.update { it.copy(editFirstName = event.value) }
            is ProfileEvent.UpdateLastName -> _state.update { it.copy(editLastName = event.value) }
            is ProfileEvent.UpdatePhoneNumber -> _state.update { it.copy(editPhoneNumber = event.value) }
            ProfileEvent.SaveProfile -> saveProfile()
            ProfileEvent.ShowDeleteDialog -> _state.update { it.copy(showDeleteDialog = true) }
            ProfileEvent.DismissDeleteDialog -> _state.update { it.copy(showDeleteDialog = false, deletePassword = "") }
            is ProfileEvent.UpdateDeletePassword -> _state.update { it.copy(deletePassword = event.value) }
            ProfileEvent.ConfirmDeleteAccount -> deleteAccount()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.getMe()
                .onSuccess { user ->
                    _state.update { it.copy(user = user, isLoading = false, error = null) }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    private fun startEditing() {
        val user = _state.value.user ?: return
        _state.update {
            it.copy(
                isEditing = true,
                editFirstName = user.firstName ?: "",
                editLastName = user.lastName ?: "",
                editPhoneNumber = user.phoneNumber ?: "",
            )
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            authRepository.updateProfile(
                firstName = s.editFirstName.ifBlank { null },
                lastName = s.editLastName.ifBlank { null },
                phoneNumber = s.editPhoneNumber.ifBlank { null },
            )
                .onSuccess { user ->
                    _state.update {
                        it.copy(user = user, isEditing = false, isSaving = false)
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isSaving = false, error = error) }
                }
        }
    }

    private fun deleteAccount() {
        val password = _state.value.deletePassword
        if (password.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            authRepository.deleteAccount(password)
                .onSuccess {
                    _state.update { it.copy(isDeleting = false, showDeleteDialog = false, accountDeleted = true) }
                    _navigation.emit(ProfileNavigationEvent.AccountDeleted)
                }
                .onError { error ->
                    _state.update { it.copy(isDeleting = false, error = error) }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            _state.update { it.copy(isLoggingOut = false, loggedOut = true) }
        }
    }
}
