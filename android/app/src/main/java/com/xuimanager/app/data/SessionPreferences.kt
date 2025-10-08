package com.xuimanager.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "xui_session"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class SessionPreferences(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.dataStore

    val data: Flow<SessionPreferencesState> = dataStore.data.map { prefs ->
        SessionPreferencesState(
            baseUrl = prefs[Keys.BASE_URL] ?: "",
            username = prefs[Keys.USERNAME] ?: "",
            enableLogging = prefs[Keys.ENABLE_LOGGING] ?: false,
            rememberSession = prefs[Keys.REMEMBER_SESSION] ?: false,
            token = prefs[Keys.TOKEN],
            adminId = prefs[Keys.ADMIN_ID]
        )
    }

    suspend fun persistLogin(
        baseUrl: String,
        username: String,
        enableLogging: Boolean,
        rememberSession: Boolean,
        token: String?,
        adminId: Int?
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.BASE_URL] = baseUrl
            prefs[Keys.USERNAME] = username
            prefs[Keys.ENABLE_LOGGING] = enableLogging
            prefs[Keys.REMEMBER_SESSION] = rememberSession
            if (rememberSession && !token.isNullOrBlank()) {
                prefs[Keys.TOKEN] = token
                if (adminId != null) {
                    prefs[Keys.ADMIN_ID] = adminId
                } else {
                    prefs.remove(Keys.ADMIN_ID)
                }
            } else {
                prefs.remove(Keys.TOKEN)
                prefs.remove(Keys.ADMIN_ID)
            }
        }
    }

    suspend fun updateRememberSession(remember: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.REMEMBER_SESSION] = remember
            if (!remember) {
                prefs.remove(Keys.TOKEN)
                prefs.remove(Keys.ADMIN_ID)
            }
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.ADMIN_ID)
        }
    }

    private object Keys {
        val BASE_URL: Preferences.Key<String> = stringPreferencesKey("base_url")
        val USERNAME: Preferences.Key<String> = stringPreferencesKey("username")
        val ENABLE_LOGGING: Preferences.Key<Boolean> = booleanPreferencesKey("enable_logging")
        val REMEMBER_SESSION: Preferences.Key<Boolean> = booleanPreferencesKey("remember_session")
        val TOKEN: Preferences.Key<String> = stringPreferencesKey("token")
        val ADMIN_ID: Preferences.Key<Int> = intPreferencesKey("admin_id")
    }
}

data class SessionPreferencesState(
    val baseUrl: String,
    val username: String,
    val enableLogging: Boolean,
    val rememberSession: Boolean,
    val token: String?,
    val adminId: Int?
)
