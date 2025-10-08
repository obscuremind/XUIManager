package com.xuimanager.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoginResponse(
    val status: Int,
    val message: String? = null,
    val token: String? = null,
    @SerialName("admin_id") val adminId: Int? = null,
    val username: String? = null
)

@Serializable
data class ResellerEnvelope(
    val status: Int,
    val message: String? = null,
    val resellers: List<JsonObject> = emptyList()
)

@Serializable
data class SubscriptionEnvelope(
    val status: Int,
    val message: String? = null,
    val subscriptions: List<JsonObject> = emptyList()
)

@Serializable
data class OnlineUsersEnvelope(
    val status: Int,
    val message: String? = null,
    val users: List<JsonObject> = emptyList()
)

@Serializable
data class ChannelWatchersEnvelope(
    val status: Int,
    val message: String? = null,
    val channels: List<JsonObject> = emptyList()
)

@Serializable
data class RenewResponse(
    val status: Int,
    val message: String? = null,
    @SerialName("subscription_id") val subscriptionId: Int? = null
)

@Serializable
data class GenericEnvelope(
    val status: Int,
    val message: String? = null,
    val error: String? = null,
    val token: String? = null,
    val data: JsonElement? = null
)

data class DisplayEntry(
    val title: String,
    val attributes: List<Pair<String, String>>
) {
    companion object {
        fun fromJson(json: JsonObject): DisplayEntry {
            val title = json["username"]?.toPrimitiveString()
                ?: json["name"]?.toPrimitiveString()
                ?: json["title"]?.toPrimitiveString()
                ?: json["id"]?.toPrimitiveString()
                ?: "Entry"
            val pairs = json.entries.map { (key, value) -> key to value.toPrimitiveString() }
            return DisplayEntry(title = title, attributes = pairs)
        }
    }
}

private fun JsonElement.toPrimitiveString(): String = when (this) {
    is JsonObject -> this.toString()
    else -> this.toString().trim('"')
}
