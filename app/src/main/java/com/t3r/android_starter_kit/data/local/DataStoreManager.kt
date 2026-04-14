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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "starter_prefs")

/**
 * Manages local persistent storage using Jetpack DataStore.
 * Stores user preferences and session flags. Auth tokens are delegated
 * to [SecureTokenStore] (EncryptedSharedPreferences) for at-rest encryption.
 *
 * Theme is exposed via an in-memory [StateFlow] ([activeTheme]) rather than
 * the raw DataStore Preferences Flow. This avoids the asynchronous
 * DataStore-write → Preferences-emission → root-recomposition cascade that
 * can destroy Nav3 entry ViewModelStores in alpha builds.
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
    // Use getAccessToken()/getRefreshToken() for synchronous reads in interceptors.
    // These flows re-emit when DataStore prefs change (e.g. after saveTokens/clearSession).

    val accessToken: Flow<String?> = context.dataStore.data.map { secureTokenStore.accessToken }
    val refreshToken: Flow<String?> = context.dataStore.data.map { secureTokenStore.refreshToken }

    /** Synchronous token access for interceptors — avoids runBlocking on a Flow. */
    fun getAccessToken(): String? = secureTokenStore.accessToken
    fun getRefreshToken(): String? = secureTokenStore.refreshToken

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

    /**
     * In-memory theme state. [MainActivity] should observe this instead of the
     * raw DataStore flow so that theme switches happen synchronously and don't
     * trigger the DataStore write → Preferences emission → deep-recomposition
     * cascade that can destroy Nav3 entry ViewModels.
     *
     * Initialised to "light"; the real value is set by [initActiveTheme].
     */
    private val _activeTheme = MutableStateFlow("light")
    val activeTheme: StateFlow<String> = _activeTheme.asStateFlow()

    /**
     * Read the persisted theme from DataStore and seed [activeTheme].
     * Call once during [MainActivity.onCreate] (before setContent).
     */
    suspend fun initActiveTheme() {
        val stored = context.dataStore.data.first()[Keys.THEME] ?: "light"
        _activeTheme.value = stored
    }

    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }

    /**
     * Update only the in-memory [activeTheme] StateFlow.
     * Triggers MaterialTheme recomposition but does NOT write to DataStore.
     * Use this as the **last** step in a settings-update flow so that the
     * companion-object cache is already populated before Nav3 recomposition
     * can recreate a ViewModel.
     */
    fun applyTheme(theme: String) {
        _activeTheme.value = theme
    }

    /**
     * Persist theme to DataStore on disk without updating [activeTheme].
     * Use when you need disk persistence without triggering recomposition.
     */
    suspend fun persistTheme(theme: String) {
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