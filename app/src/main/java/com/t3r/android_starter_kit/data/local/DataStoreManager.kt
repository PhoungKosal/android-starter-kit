package com.t3r.android_starter_kit.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "starter_prefs")

/**
 * Manages local persistent storage using Jetpack DataStore.
 * Stores user preferences and session flags. Auth tokens are delegated
 * to [SecureTokenStore] (EncryptedSharedPreferences) for at-rest encryption.
 */
@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureTokenStore: SecureTokenStore,
) {
    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    // -- Auth Tokens (encrypted via SecureTokenStore) --

    val accessToken: Flow<String?> = context.dataStore.data.map { secureTokenStore.accessToken }
    val refreshToken: Flow<String?> = context.dataStore.data.map { secureTokenStore.refreshToken }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val userId: Flow<String?> = context.dataStore.data.map { it[Keys.USER_ID] }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        secureTokenStore.accessToken = accessToken
        secureTokenStore.refreshToken = refreshToken
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { it[Keys.USER_ID] = userId }
    }

    suspend fun clearSession() {
        secureTokenStore.clearTokens()
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }

    // -- Preferences --

    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "system" }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[Keys.THEME] = theme }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    // -- FCM Token --

    val fcmToken: Flow<String?> = context.dataStore.data.map { it[Keys.FCM_TOKEN] }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { it[Keys.FCM_TOKEN] = token }
    }
}