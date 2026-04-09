package com.t3r.android_starter_kit.core.state

/**
 * One-shot UI events that should be consumed once (e.g., navigation, snack bar).
 */
sealed interface UiEvent {
    data class Navigate(val route: String) : UiEvent
    data object NavigateBack : UiEvent
}
