package com.xuimanager.app.data

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class XuiRepository(private val service: XuiService) {
    private var token: String? = null

    suspend fun login(username: String, password: String): LoginResult = safeCall {
        val response = service.login(username = username, password = password)
        if (response.status != 1 || response.token.isNullOrBlank()) {
            val message = response.message ?: "Login failed"
            throw XuiRepositoryException(message)
        }
        token = response.token
        LoginResult(
            token = response.token,
            adminId = response.adminId,
            username = response.username
        )
    }

    suspend fun fetchResellers(): List<DisplayEntry> = safeCall {
        val result = service.getResellers(token = requireToken())
        if (result.status != 1) {
            throw XuiRepositoryException(result.message ?: "Unable to load resellers")
        }
        result.resellers.map { DisplayEntry.fromJson(it) }
    }

    suspend fun fetchSubscriptions(status: String?): List<DisplayEntry> = safeCall {
        val result = service.getSubscriptions(token = requireToken(), status = status?.ifBlank { null })
        if (result.status != 1) {
            throw XuiRepositoryException(result.message ?: "Unable to load subscriptions")
        }
        result.subscriptions.map { DisplayEntry.fromJson(it) }
    }

    suspend fun renewSubscription(subscriptionId: Int, months: Int): RenewResult = safeCall {
        val safeMonths = months.coerceAtLeast(1)
        val result = service.renewSubscription(
            token = requireToken(),
            subscriptionId = subscriptionId,
            months = safeMonths
        )
        if (result.status != 1) {
            throw XuiRepositoryException(result.message ?: "Renewal failed")
        }
        RenewResult(message = result.message ?: "Subscription renewed", subscriptionId = result.subscriptionId)
    }

    suspend fun fetchOnlineUsers(): List<DisplayEntry> = safeCall {
        val result = service.getOnlineUsers(token = requireToken())
        if (result.status != 1) {
            throw XuiRepositoryException(result.message ?: "Unable to load online users")
        }
        result.users.map { DisplayEntry.fromJson(it) }
    }

    suspend fun fetchChannelWatchers(channelId: Int?): List<DisplayEntry> = safeCall {
        val result = service.getChannelWatchers(token = requireToken(), channelId = channelId)
        if (result.status != 1) {
            throw XuiRepositoryException(result.message ?: "Unable to load channel watchers")
        }
        result.channels.map { DisplayEntry.fromJson(it) }
    }

    suspend fun runCustomAction(params: Map<String, String>): GenericResult = safeCall {
        val action = params["action"]?.trim()?.takeIf { it.isNotBlank() }
            ?: throw XuiRepositoryException("Action name is required")
        val payload = params.toMutableMap().apply {
            this["action"] = action
            this["token"] = requireToken()
        }
        val result = service.customAction(payload)
        if (result.status != 1) {
            throw XuiRepositoryException(result.message ?: result.error ?: "Custom action failed")
        }
        GenericResult(message = result.message, token = result.token)
    }

    fun clearToken() {
        token = null
    }

    fun restoreToken(existingToken: String) {
        token = existingToken
    }

    private fun requireToken(): String = token ?: throw IllegalStateException("Not authenticated")

    private suspend fun <T> safeCall(block: suspend () -> T): T {
        return try {
            block()
        } catch (ex: XuiRepositoryException) {
            throw ex
        } catch (ex: HttpException) {
            throw XuiRepositoryException("Server error ${ex.code()}")
        } catch (ex: SerializationException) {
            throw XuiRepositoryException("Failed to parse server response")
        } catch (ex: IOException) {
            throw XuiRepositoryException(ex.message ?: "Network error, please check your connection")
        }
    }
}

data class LoginResult(
    val token: String,
    val adminId: Int?,
    val username: String?
)

data class RenewResult(
    val message: String,
    val subscriptionId: Int?
)

data class GenericResult(
    val message: String?,
    val token: String?
)

class XuiRepositoryException(message: String) : Exception(message)
