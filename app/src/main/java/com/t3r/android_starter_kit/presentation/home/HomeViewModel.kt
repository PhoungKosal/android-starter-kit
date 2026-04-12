package com.t3r.android_starter_kit.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3r.android_starter_kit.core.result.onError
import com.t3r.android_starter_kit.core.result.onSuccess
import com.t3r.android_starter_kit.data.remote.socket.NotificationSocketEvent
import com.t3r.android_starter_kit.data.remote.socket.NotificationSocketManager
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationsRepository: NotificationsRepository,
    private val socketManager: NotificationSocketManager,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadData()
        observeSocketEvents()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadProfile -> loadData()
            HomeEvent.Refresh -> loadData()
        }
    }

    private fun observeSocketEvents() {
        viewModelScope.launch {
            socketManager.events.collect { event ->
                when (event) {
                    is NotificationSocketEvent.NewNotification -> refreshUnreadCount()
                    is NotificationSocketEvent.Read -> {
                        event.unreadCount?.let { count ->
                            _state.update { it.copy(unreadNotificationCount = count) }
                        } ?: refreshUnreadCount()
                    }
                    is NotificationSocketEvent.ReadAll -> {
                        _state.update { it.copy(unreadNotificationCount = event.unreadCount) }
                    }
                    is NotificationSocketEvent.Removed -> {
                        event.unreadCount?.let { count ->
                            _state.update { it.copy(unreadNotificationCount = count) }
                        } ?: refreshUnreadCount()
                    }
                }
            }
        }
    }

    private fun refreshUnreadCount() {
        viewModelScope.launch {
            notificationsRepository.getUnreadCount()
                .onSuccess { count ->
                    _state.update { it.copy(unreadNotificationCount = count) }
                }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Load user profile
            authRepository.getMe()
                .onSuccess { user ->
                    _state.update { it.copy(user = user, isLoading = false, error = null) }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }

            // Load unread notification count
            notificationsRepository.getUnreadCount()
                .onSuccess { count ->
                    _state.update { it.copy(unreadNotificationCount = count) }
                }
        }
    }
}
