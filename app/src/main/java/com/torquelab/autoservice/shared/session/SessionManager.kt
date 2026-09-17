package com.torquelab.autoservice.shared.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(
    name = "autoservice_session"
)

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _session = MutableStateFlow<UserSession?>(null)

    val session = _session.asStateFlow()

    val currentToken: String?
        get() = _session.value?.token

    suspend fun saveSession(session: UserSession) {
        context.sessionDataStore.edit { preferences ->

            preferences[USER_ID] = session.userId
            preferences[EMAIL] = session.email
            preferences[ROLE] = session.role
            preferences[TOKEN] = session.token

            if (session.workshopId != null) {
                preferences[WORKSHOP_ID] = session.workshopId
            } else {
                preferences.remove(WORKSHOP_ID)
            }

            if (session.mechanicId != null) {
                preferences[MECHANIC_ID] = session.mechanicId
            } else {
                preferences.remove(MECHANIC_ID)
            }
        }

        _session.value = session
    }

    suspend fun restoreSession(): UserSession? {
        val preferences = context.sessionDataStore.data.first()

        val token = preferences[TOKEN] ?: return null
        val userId = preferences[USER_ID] ?: return null
        val email = preferences[EMAIL] ?: return null
        val role = preferences[ROLE] ?: return null

        val restoredSession = UserSession(
            userId = userId,
            email = email,
            role = role,
            workshopId = preferences[WORKSHOP_ID],
            mechanicId = preferences[MECHANIC_ID],
            token = token
        )

        _session.value = restoredSession

        return restoredSession
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }

        _session.value = null
    }

    fun isAuthenticated(): Boolean {
        return currentToken != null
    }

    fun currentSession(): UserSession? {
        return _session.value
    }

    companion object {
        private val USER_ID = intPreferencesKey("user_id")
        private val EMAIL = stringPreferencesKey("email")
        private val ROLE = stringPreferencesKey("role")
        private val WORKSHOP_ID = stringPreferencesKey("workshop_id")
        private val MECHANIC_ID = intPreferencesKey("mechanic_id")
        private val TOKEN = stringPreferencesKey("token")
    }
}