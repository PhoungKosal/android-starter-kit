package com.t3r.android_starter_kit.core.locale

import com.t3r.android_starter_kit.data.local.DataStoreManager
import com.t3r.android_starter_kit.domain.model.User
import kotlinx.coroutines.flow.first

/**
 * Syncs the user's locale and theme preferences from the server to local storage.
 * Called from HomeViewModel and SettingsViewModel after loading the user profile.
 */
object UserPreferenceSync {

    /**
     * @return the resolved server locale after syncing.
     */
    suspend fun syncLocale(user: User, dataStoreManager: DataStoreManager): LocaleManager.AppLocale {
        val serverLocale = LocaleManager.AppLocale.fromTag(user.language)
        if (serverLocale != LocaleManager.getCurrentLocale()) {
            LocaleManager.setLocale(serverLocale)
            dataStoreManager.saveLanguage(serverLocale.tag)
        }
        return serverLocale
    }

    /**
     * @return the resolved theme value ("light", "dark", or "system") after syncing.
     */
    suspend fun syncTheme(user: User, dataStoreManager: DataStoreManager): String {
        val localThemeValue = dataStoreManager.theme.first()
        return if (localThemeValue == "system") {
            "system"
        } else {
            val serverTheme = user.theme ?: localThemeValue
            dataStoreManager.saveTheme(serverTheme)
            serverTheme
        }
    }
}
