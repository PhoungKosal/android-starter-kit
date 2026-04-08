package com.t3r.android_starter_kit.core.state

/**
 * One-shot UI events that should be consumed once (e.g., navigation, snackbar).
 */
sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class Navigate(val route: String) : UiEvent
    data object NavigateBack : UiEvent
}
