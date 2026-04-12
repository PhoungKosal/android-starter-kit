package com.t3r.android_starter_kit.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3r.android_starter_kit.core.result.onError
import com.t3r.android_starter_kit.core.result.onSuccess
import com.t3r.android_starter_kit.data.remote.socket.NotificationSocketEvent
import com.t3r.android_starter_kit.data.remote.socket.NotificationSocketManager
import com.t3r.android_starter_kit.domain.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val socketManager: NotificationSocketManager,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        load()
        observeSocketEvents()
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.Load -> load()
            NotificationsEvent.LoadMore -> loadMore()
            NotificationsEvent.Refresh -> load()
            is NotificationsEvent.MarkAsRead -> markAsRead(event.id)
            NotificationsEvent.MarkAllAsRead -> markAllAsRead()
            is NotificationsEvent.Delete -> delete(event.id)
        }
    }

    private fun observeSocketEvents() {
        viewModelScope.launch {
            socketManager.events.collect { event ->
                when (event) {
                    is NotificationSocketEvent.NewNotification -> load()
                    is NotificationSocketEvent.Read -> {
                        event.notificationId?.let { id ->
                            _state.update { state ->
                                state.copy(
                                    notifications = state.notifications.map {
                                        if (it.id == id) it.copy(isRead = true) else it
                                    },
                                )
                            }
                        }
                    }
                    is NotificationSocketEvent.ReadAll -> {
                        _state.update { state ->
                            state.copy(
                                notifications = state.notifications.map { it.copy(isRead = true) },
                            )
                        }
                    }
                    is NotificationSocketEvent.Removed -> {
                        event.notificationId?.let { id ->
                            _state.update { state ->
                                state.copy(
                                    notifications = state.notifications.filter { it.id != id },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            notificationsRepository.getNotifications(page = 1)
                .onSuccess { paginated ->
                    _state.update {
                        it.copy(
                            notifications = paginated.data,
                            currentPage = paginated.currentPage,
                            hasMore = paginated.hasMore,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    private fun loadMore() {
        val currentState = _state.value
        if (!currentState.hasMore || currentState.isLoadingMore) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            notificationsRepository.getNotifications(page = currentState.currentPage + 1)
                .onSuccess { paginated ->
                    _state.update {
                        it.copy(
                            notifications = it.notifications + paginated.data,
                            currentPage = paginated.currentPage,
                            hasMore = paginated.hasMore,
                            isLoadingMore = false,
                        )
                    }
                }
                .onError {
                    _state.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    private fun markAsRead(id: String) {
        viewModelScope.launch {
            // Optimistic update — reflect the change instantly in the UI
            _state.update { state ->
                state.copy(
                    notifications = state.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                )
            }

            notificationsRepository.markAsRead(id)
                .onSuccess { updatedNotification ->
                    // Replace with the server-authoritative version
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == id) updatedNotification else it
                            }
                        )
                    }
                }
                .onError {
                    // Revert the optimistic update on failure
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == id) it.copy(isRead = false) else it
                            }
                        )
                    }
                }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            notificationsRepository.markAllAsRead()
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) }
                        )
                    }
                }
        }
    }

    private fun delete(id: String) {
        viewModelScope.launch {
            notificationsRepository.deleteNotification(id)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.filter { it.id != id }
                        )
                    }
                }
        }
    }
}
