package com.xuimanager.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xuimanager.app.data.DisplayEntry
import com.xuimanager.app.data.LoginResult
import com.xuimanager.app.data.RenewResult
import com.xuimanager.app.data.SessionPreferences
import com.xuimanager.app.data.SessionPreferencesState
import com.xuimanager.app.data.XuiRepository
import com.xuimanager.app.data.XuiRepositoryException
import com.xuimanager.app.data.XuiServiceFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class XuiViewModel(private val preferences: SessionPreferences) : ViewModel() {
    var uiState by mutableStateOf(XuiUiState())
        private set

    private var repository: XuiRepository? = null
    private var isRestoringSession = false

    init {
        viewModelScope.launch {
            preferences.data.collectLatest { state ->
                uiState = uiState.copy(
                    loginForm = uiState.loginForm.copy(
                        baseUrl = state.baseUrl,
                        username = state.username,
                        enableLogging = state.enableLogging,
                        rememberSession = state.rememberSession
                    )
                )
                if (!isRestoringSession && state.shouldRestoreSession() && repository == null) {
                    restoreSession(state)
                }
            }
        }
    }

    fun login(baseUrl: String, username: String, password: String, enableLogging: Boolean) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, successMessage = null)
            try {
                val service = XuiServiceFactory.create(baseUrl, enableLogging)
                val repo = XuiRepository(service)
                val result = repo.login(username, password)
                repository = repo
                uiState = uiState.copy(
                    isLoading = false,
                    error = null,
                    loginResult = result,
                    baseUrl = baseUrl,
                    successMessage = "Logged in successfully",
                    loginForm = uiState.loginForm.copy(
                        baseUrl = baseUrl,
                        enableLogging = enableLogging,
                        username = username
                    )
                )
                persistSessionPreferences(baseUrl, username, enableLogging, result)
                refreshAll()
            } catch (ex: Exception) {
                uiState = uiState.copy(isLoading = false, error = ex.message ?: "Unexpected error")
            }
        }
    }

    fun refreshAll() {
        val repo = repository ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val statusFilter = uiState.subscriptionStatus
                val channelIdFilter = uiState.channelFilter?.toIntOrNull()
                val resellersDeferred = async { repo.fetchResellers() }
                val subscriptionsDeferred = async { repo.fetchSubscriptions(statusFilter) }
                val usersDeferred = async { repo.fetchOnlineUsers() }
                val channelsDeferred = async { repo.fetchChannelWatchers(channelIdFilter) }
                val resellers = resellersDeferred.await()
                val subscriptions = subscriptionsDeferred.await()
                val users = usersDeferred.await()
                val channels = channelsDeferred.await()
                uiState = uiState.copy(
                    isLoading = false,
                    resellers = resellers,
                    subscriptions = subscriptions,
                    onlineUsers = users,
                    channelWatchers = channels,
                    error = null
                )
            } catch (ex: Exception) {
                uiState = uiState.copy(isLoading = false, error = ex.message ?: "Unable to refresh data")
            }
        }
    }

    fun renewSubscription(subscriptionId: Int, months: Int) {
        val repo = repository ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isRenewing = true, error = null, successMessage = null)
            try {
                val result: RenewResult = repo.renewSubscription(subscriptionId, months)
                uiState = uiState.copy(
                    isRenewing = false,
                    successMessage = result.message,
                    error = null
                )
                refreshAll()
            } catch (ex: Exception) {
                uiState = uiState.copy(isRenewing = false, error = ex.message ?: "Unable to renew subscription")
            }
        }
    }

    fun updateSubscriptionStatus(status: String?) {
        uiState = uiState.copy(subscriptionStatus = status?.takeIf { it.isNotBlank() })
        refreshAll()
    }

    fun updateChannelFilter(channelId: String?) {
        uiState = uiState.copy(channelFilter = channelId?.takeIf { it.isNotBlank() })
        refreshAll()
    }

    fun runCustomAction(params: Map<String, String>) {
        val repo = repository ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, successMessage = null)
            try {
                val result = repo.runCustomAction(params)
                uiState = uiState.copy(
                    isLoading = false,
                    successMessage = result.message ?: "Action completed",
                    error = null
                )
            } catch (ex: XuiRepositoryException) {
                uiState = uiState.copy(isLoading = false, error = ex.message)
            } catch (ex: Exception) {
                uiState = uiState.copy(isLoading = false, error = ex.message ?: "Failed to run custom action")
            }
        }
    }

    fun logout() {
        repository?.clearToken()
        repository = null
        val form = uiState.loginForm
        uiState = XuiUiState(loginForm = form)
        viewModelScope.launch { preferences.clearToken() }
    }

    fun clearError() {
        if (uiState.error != null) {
            uiState = uiState.copy(error = null)
        }
    }

    fun clearSuccessMessage() {
        if (uiState.successMessage != null) {
            uiState = uiState.copy(successMessage = null)
        }
    }

    fun updateBaseUrl(value: String) {
        updateLoginForm { it.copy(baseUrl = value) }
    }

    fun updateUsername(value: String) {
        updateLoginForm { it.copy(username = value) }
    }

    fun updateLoggingPreference(enabled: Boolean) {
        updateLoginForm { it.copy(enableLogging = enabled) }
    }

    fun updateRememberSession(remember: Boolean) {
        updateLoginForm { it.copy(rememberSession = remember) }
        viewModelScope.launch { preferences.updateRememberSession(remember) }
    }

    private fun updateLoginForm(transform: (LoginFormState) -> LoginFormState) {
        uiState = uiState.copy(loginForm = transform(uiState.loginForm))
    }

    private fun persistSessionPreferences(
        baseUrl: String,
        username: String,
        enableLogging: Boolean,
        result: LoginResult
    ) {
        val form = uiState.loginForm
        viewModelScope.launch {
            preferences.persistLogin(
                baseUrl = baseUrl,
                username = username,
                enableLogging = enableLogging,
                rememberSession = form.rememberSession,
                token = if (form.rememberSession) result.token else null,
                adminId = result.adminId
            )
        }
    }

    private fun restoreSession(prefs: SessionPreferencesState) {
        isRestoringSession = true
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null, successMessage = null)
                val service = XuiServiceFactory.create(prefs.baseUrl, prefs.enableLogging)
                val repo = XuiRepository(service)
                val token = prefs.token ?: return@launch
                repo.restoreToken(token)
                repository = repo
                val loginResult = LoginResult(
                    token = token,
                    adminId = prefs.adminId,
                    username = prefs.username.ifBlank { null }
                )
                uiState = uiState.copy(
                    isLoading = false,
                    loginResult = loginResult,
                    baseUrl = prefs.baseUrl,
                    successMessage = "Session restored"
                )
                refreshAll()
            } catch (ex: Exception) {
                repository = null
                uiState = uiState.copy(isLoading = false, error = ex.message ?: "Failed to restore session")
                preferences.clearToken()
            } finally {
                isRestoringSession = false
            }
        }
    }
}

data class XuiUiState(
    val loginForm: LoginFormState = LoginFormState(),
    val baseUrl: String = "",
    val loginResult: LoginResult? = null,
    val resellers: List<DisplayEntry> = emptyList(),
    val subscriptions: List<DisplayEntry> = emptyList(),
    val onlineUsers: List<DisplayEntry> = emptyList(),
    val channelWatchers: List<DisplayEntry> = emptyList(),
    val subscriptionStatus: String? = null,
    val channelFilter: String? = null,
    val isLoading: Boolean = false,
    val isRenewing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class LoginFormState(
    val baseUrl: String = "",
    val username: String = "",
    val enableLogging: Boolean = false,
    val rememberSession: Boolean = false
)

private fun SessionPreferencesState.shouldRestoreSession(): Boolean {
    return rememberSession && !token.isNullOrBlank() && baseUrl.isNotBlank()
}
