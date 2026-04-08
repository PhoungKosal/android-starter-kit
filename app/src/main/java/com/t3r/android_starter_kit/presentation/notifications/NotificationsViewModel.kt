package com.t3r.android_starter_kit.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starterkit.app.core.result.onError
import com.starterkit.app.core.result.onSuccess
import com.starterkit.app.domain.repository.NotificationsRepository
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
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.Load -> load()
            NotificationsEvent.LoadMore -> loadMore()
            NotificationsEvent.Refresh -> refresh()
            is NotificationsEvent.MarkAsRead -> markAsRead(event.id)
            NotificationsEvent.MarkAllAsRead -> markAllAsRead()
            is NotificationsEvent.Delete -> delete(event.id)
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

    private fun refresh() {
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

    private fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationsRepository.markAsRead(id)
                .onSuccess { updatedNotification ->
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == id) updatedNotification else it
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
