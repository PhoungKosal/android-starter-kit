package com.t3r.android_starter_kit.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3r.android_starter_kit.core.locale.LocaleManager
import com.t3r.android_starter_kit.core.result.onError
import com.t3r.android_starter_kit.core.result.onSuccess
import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        load()
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
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.getMe()
                .onSuccess { user ->
                    // Sync locale
                    val serverLocale = LocaleManager.AppLocale.fromTag(user.language)
                    if (serverLocale != LocaleManager.getCurrentLocale()) {
                        LocaleManager.setLocale(serverLocale)
                        dataStoreManager.saveLanguage(serverLocale.tag)
                    }
                    // Sync theme
                    val localTheme = dataStoreManager.theme.first()
                    val themeValue = if (localTheme == "system") "system" else (user.theme ?: localTheme)
                    if (localTheme != "system") dataStoreManager.saveTheme(themeValue)
                    val currentTheme = AppTheme.fromString(themeValue)

                    _state.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            error = null,
                            currentLocale = serverLocale,
                            currentTheme = currentTheme,
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                }
        }
    }

    private fun changeLocale(locale: LocaleManager.AppLocale) {
        LocaleManager.setLocale(locale)
        _state.update { it.copy(currentLocale = locale, showLanguagePicker = false) }

        viewModelScope.launch {
            dataStoreManager.saveLanguage(locale.tag)
            // Sync to server
            authRepository.updateMySettings(language = locale.tag)
                .onError { error ->
                    Timber.w("Failed to sync language to server: ${error.message}")
                }
        }
    }

    private fun changeTheme(theme: AppTheme) {
        _state.update { it.copy(currentTheme = theme, showThemePicker = false) }

        viewModelScope.launch {
            dataStoreManager.saveTheme(theme.value)
            // Only sync "light" / "dark" to server; "system" is client-only
            if (theme != AppTheme.SYSTEM) {
                authRepository.updateMySettings(theme = theme.value)
                    .onError { error ->
                        Timber.w("Failed to sync theme to server: ${error.message}")
                    }
            }
        }
    }
}
