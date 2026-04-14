package com.t3r.android_starter_kit.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3r.android_starter_kit.core.locale.LocaleManager
import com.t3r.android_starter_kit.core.result.onError
import com.t3r.android_starter_kit.core.result.onSuccess
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.domain.model.User
import com.t3r.android_starter_kit.domain.model.UserSettings
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * SettingsViewModel — follows the same data flow as the Vue admin dashboard:
 *
 * READ  → GET /users/settings  → full [UserSettings]
 * WRITE → PATCH /users/settings → re-fetch full settings (invalidateQueries)
 *
 * Theme is applied via [DataStoreManager.applyTheme] (in-memory StateFlow)
 * which MainActivity observes. The visual theme change is always the **last**
 * operation after all API + DataStore work completes. This ensures the
 * companion-object cache is populated before Nav3 recomposition can recreate
 * this ViewModel — so the user never sees a loading spinner after a theme change.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {

    companion object {
        /**
         * In-memory cache shared across ViewModel instances. When a theme
         * change triggers MaterialTheme recomposition and Nav3 (alpha)
         * recreates this ViewModel, the init block reads from the cache
         * so the user never sees a loading spinner.
         */
        @Volatile
        private var cachedUser: User? = null

        @Volatile
        private var cachedSettings: UserSettings? = null

        private fun updateCache(user: User?, settings: UserSettings) {
            cachedUser = user
            cachedSettings = settings
        }

        /** Call on logout to avoid leaking data across sessions. */
        fun clearCache() {
            cachedUser = null
            cachedSettings = null
        }
    }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    /** Tracks the current settings-write operation so rapid taps cancel the previous one. */
    private var settingsJob: Job? = null

    init {
        val user = cachedUser
        val settings = cachedSettings
        if (user != null && settings != null) {
            // Restore from cache — avoids a loading spinner when Nav3
            // recreates this ViewModel after a theme-change recomposition.
            _state.update {
                it.copy(
                    user = user,
                    settings = settings,
                    isLoading = false,
                    error = null,
                    currentTheme = AppTheme.fromString(settings.theme),
                    currentLocale = LocaleManager.AppLocale.fromTag(settings.language),
                    emailNotifications = settings.emailNotifications,
                    pushNotifications = settings.pushNotifications,
                )
            }
            // Background refresh without loading spinner.
            silentLoad()
        } else {
            load()
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.Load -> load()
            SettingsEvent.ShowLanguagePicker -> _state.update { it.copy(showLanguagePicker = true) }
            SettingsEvent.DismissLanguagePicker -> _state.update { it.copy(showLanguagePicker = false) }
            is SettingsEvent.ChangeLocale -> changeLocale(event.locale)
            SettingsEvent.ShowThemePicker -> _state.update { it.copy(showThemePicker = true) }
            SettingsEvent.DismissThemePicker -> _state.update { it.copy(showThemePicker = false) }
            is SettingsEvent.ChangeTheme -> changeTheme(event.theme)
            is SettingsEvent.ToggleEmailNotifications -> toggleEmailNotifications(event.enabled)
            is SettingsEvent.TogglePushNotifications -> togglePushNotifications(event.enabled)
        }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            authRepository.getMe()
                .onSuccess { user -> _state.update { it.copy(user = user) } }
                .onError { error -> _state.update { it.copy(isLoading = false, error = error) }; return@launch }

            authRepository.getMySettings()
                .onSuccess { settings ->
                    val serverLocale = LocaleManager.AppLocale.fromTag(settings.language)
                    if (serverLocale != LocaleManager.getCurrentLocale()) {
                        LocaleManager.setLocale(serverLocale)
                    }
                    dataStoreManager.saveLanguage(settings.language)

                    _state.update {
                        it.copy(
                            settings = settings,
                            isLoading = false,
                            error = null,
                            currentLocale = serverLocale,
                            currentTheme = AppTheme.fromString(settings.theme),
                            emailNotifications = settings.emailNotifications,
                            pushNotifications = settings.pushNotifications,
                        )
                    }

                    updateCache(_state.value.user, settings)

                    // Persist + apply theme LAST. persistTheme writes to
                    // DataStore without touching the StateFlow. applyTheme
                    // updates the in-memory StateFlow that MainActivity
                    // observes — may trigger a MaterialTheme recomposition,
                    // but the cache is already populated above.
                    dataStoreManager.persistTheme(settings.theme)
                    dataStoreManager.applyTheme(settings.theme)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    /**
     * Background refresh that never shows a loading spinner.
     * Used when the companion-object cache already provides displayable data.
     */
    private fun silentLoad() {
        viewModelScope.launch {
            authRepository.getMe()
                .onSuccess { user -> _state.update { it.copy(user = user) } }
                .onError { return@launch }

            authRepository.getMySettings()
                .onSuccess { settings ->
                    val serverLocale = LocaleManager.AppLocale.fromTag(settings.language)
                    if (serverLocale != LocaleManager.getCurrentLocale()) {
                        LocaleManager.setLocale(serverLocale)
                    }
                    dataStoreManager.saveLanguage(settings.language)

                    _state.update {
                        it.copy(
                            settings = settings,
                            currentLocale = serverLocale,
                            currentTheme = AppTheme.fromString(settings.theme),
                            emailNotifications = settings.emailNotifications,
                            pushNotifications = settings.pushNotifications,
                        )
                    }

                    updateCache(_state.value.user, settings)
                    dataStoreManager.persistTheme(settings.theme)
                    dataStoreManager.applyTheme(settings.theme)
                }
        }
    }

    /**
     * Re-fetch full settings from the server (like Vue's `invalidateQueries`).
     * Updates state, cache, and DataStore. Does **not** apply theme visually —
     * callers must call [DataStoreManager.applyTheme] explicitly after this
     * returns so the recomposition happens only when the cache is ready.
     */
    private suspend fun refreshSettings() {
        authRepository.getMySettings()
            .onSuccess { settings ->
                val locale = LocaleManager.AppLocale.fromTag(settings.language)
                _state.update {
                    it.copy(
                        settings = settings,
                        currentTheme = AppTheme.fromString(settings.theme),
                        currentLocale = locale,
                        emailNotifications = settings.emailNotifications,
                        pushNotifications = settings.pushNotifications,
                        isSaving = false,
                    )
                }

                updateCache(_state.value.user, settings)

                // Persist to DataStore without changing the in-memory theme.
                dataStoreManager.persistTheme(settings.theme)
                dataStoreManager.saveLanguage(settings.language)

                // Locale LAST (may trigger Activity config change).
                if (locale != LocaleManager.getCurrentLocale()) {
                    LocaleManager.setLocale(locale)
                }
            }
            .onError {
                _state.update { it.copy(isSaving = false) }
            }
    }

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Change language — PATCH then re-fetch.
     *
     * Wraps work in `withContext(NonCancellable)` so the operation completes
     * even if the Activity is recreated by locale config change.
     */
    private fun changeLocale(locale: LocaleManager.AppLocale) {
        if (_state.value.isSaving) return
        settingsJob?.cancel()
        _state.update {
            it.copy(
                currentLocale = locale,
                showLanguagePicker = false,
                isSaving = true,
            )
        }

        settingsJob = viewModelScope.launch {
            withContext(NonCancellable) {
                authRepository.updateMySettings(language = locale.tag)
                    .onSuccess { refreshSettings() }
                    .onError { error ->
                        Timber.w("Failed to sync language to server: ${error.message}")
                        val previous = _state.value.settings?.language
                        val revertLocale = LocaleManager.AppLocale.fromTag(previous)
                        _state.update {
                            it.copy(currentLocale = revertLocale, isSaving = false)
                        }
                    }
            }
        }
    }

    /**
     * Change theme — PATCH, re-fetch, then apply theme LAST.
     *
     * The visual theme change ([DataStoreManager.applyTheme]) is the absolute
     * last step, **after** the companion-object cache has been populated by
     * [refreshSettings]. If the resulting MaterialTheme recomposition causes
     * Nav3 to recreate this ViewModel, the new instance reads the cache in
     * the init block and displays data immediately — no loading spinner.
     *
     * Wraps work in `withContext(NonCancellable)` so the operation completes
     * even if the ViewModel is destroyed mid-flight by the theme recomposition.
     */
    private fun changeTheme(theme: AppTheme) {
        if (_state.value.isSaving) return
        settingsJob?.cancel()
        _state.update {
            it.copy(
                currentTheme = theme,
                showThemePicker = false,
                isSaving = true,
            )
        }

        settingsJob = viewModelScope.launch {
            withContext(NonCancellable) {
                authRepository.updateMySettings(theme = theme.value)
                    .onSuccess {
                        // 1. Re-fetch + cache + persist (no visual recomp yet).
                        refreshSettings()
                        // 2. Apply theme — triggers MaterialTheme recomposition.
                        //    All API work is done; cache is populated.
                        dataStoreManager.applyTheme(theme.value)
                    }
                    .onError { error ->
                        Timber.w("Failed to sync theme to server: ${error.message}")
                        val previousTheme = _state.value.settings?.theme ?: "light"
                        _state.update {
                            it.copy(
                                currentTheme = AppTheme.fromString(previousTheme),
                                isSaving = false,
                            )
                        }
                    }
            }
        }
    }

    /**
     * Toggle email notifications — optimistic update, PATCH, re-fetch.
     * Matches the Vue notifications page pattern (toggle → PATCH immediately).
     */
    private fun toggleEmailNotifications(enabled: Boolean) {
        if (_state.value.isSaving) return
        settingsJob?.cancel()
        val previous = _state.value.emailNotifications
        _state.update { it.copy(emailNotifications = enabled, isSaving = true) }

        settingsJob = viewModelScope.launch {
            withContext(NonCancellable) {
                authRepository.updateMySettings(emailNotifications = enabled)
                    .onSuccess { refreshSettings() }
                    .onError { error ->
                        Timber.w("Failed to toggle email notifications: ${error.message}")
                        _state.update { it.copy(emailNotifications = previous, isSaving = false) }
                    }
            }
        }
    }

    /**
     * Toggle push notifications — optimistic update, PATCH, re-fetch.
     */
    private fun togglePushNotifications(enabled: Boolean) {
        if (_state.value.isSaving) return
        settingsJob?.cancel()
        val previous = _state.value.pushNotifications
        _state.update { it.copy(pushNotifications = enabled, isSaving = true) }

        settingsJob = viewModelScope.launch {
            withContext(NonCancellable) {
                authRepository.updateMySettings(pushNotifications = enabled)
                    .onSuccess { refreshSettings() }
                    .onError { error ->
                        Timber.w("Failed to toggle push notifications: ${error.message}")
                        _state.update { it.copy(pushNotifications = previous, isSaving = false) }
                    }
            }
        }
    }
}
