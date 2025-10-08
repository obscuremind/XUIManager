package com.xuimanager.app.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType

interface XuiService {
    @FormUrlEncoded
    @POST("reseller_api.php")
    suspend fun login(
        @Field("action") action: String = "login",
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("reseller_api.php")
    suspend fun getResellers(
        @Field("action") action: String = "get_resellers",
        @Field("token") token: String
    ): ResellerEnvelope

    @FormUrlEncoded
    @POST("reseller_api.php")
    suspend fun getSubscriptions(
        @Field("action") action: String = "get_subscriptions",
        @Field("token") token: String,
        @Field("status") status: String? = null
    ): SubscriptionEnvelope

    @FormUrlEncoded
    @POST("reseller_api.php")
    suspend fun renewSubscription(
        @Field("action") action: String = "renew_subscription",
        @Field("token") token: String,
        @Field("subscription_id") subscriptionId: Int,
        @Field("months") months: Int
    ): RenewResponse

    @FormUrlEncoded
    @POST("reseller_api.php")
    suspend fun getOnlineUsers(
        @Field("action") action: String = "get_online_users",
        @Field("token") token: String
    ): OnlineUsersEnvelope

    @FormUrlEncoded
    @POST("reseller_api.php")
    suspend fun getChannelWatchers(
        @Field("action") action: String = "get_channel_watchers",
        @Field("token") token: String,
        @Field("channel_id") channelId: Int? = null
    ): ChannelWatchersEnvelope

    @FormUrlEncoded
    @POST("reseller_api.php")
    suspend fun customAction(
        @FieldMap params: Map<String, String>
    ): GenericEnvelope
}

object XuiServiceFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    fun create(rawBaseUrl: String, enableLogging: Boolean = false): XuiService {
        val logging = HttpLoggingInterceptor().apply {
            level = if (enableLogging) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(sanitiseBaseUrl(rawBaseUrl))
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(client)
            .build()
            .create(XuiService::class.java)
    }

    private fun sanitiseBaseUrl(baseUrl: String): String {
        var working = baseUrl.trim()
        if (!working.startsWith("http://") && !working.startsWith("https://")) {
            working = "https://$working"
        }
        if (!working.endsWith('/')) {
            working += '/'
        }
        return working
    }
}
