package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

// --- Supabase REST Models ---

@JsonClass(generateAdapter = true)
data class SupabaseAuthRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "data") val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "token_type") val tokenType: String?,
    @Json(name = "expires_in") val expiresIn: Long?,
    @Json(name = "user") val user: SupabaseUser?
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String?,
    @Json(name = "user_metadata") val userMetadata: Map<String, String>?
)

@JsonClass(generateAdapter = true)
data class SupabasePostModel(
    @Json(name = "id") val id: String,
    @Json(name = "caption") val caption: String,
    @Json(name = "type") val type: String,
    @Json(name = "content_url") val contentUrl: String,
    @Json(name = "category") val category: String,
    @Json(name = "hashtags") val hashtags: String,
    @Json(name = "likes_count") val likesCount: Int,
    @Json(name = "comments_count") val commentsCount: Int,
    @Json(name = "author_handle") val authorHandle: String
)

// --- Retrofit Interface ---

interface SupabaseService {

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthRequest
    ): SupabaseAuthResponse

    @POST("auth/v1/token")
    suspend fun signIn(
        @Header("apikey") apiKey: String,
        @Query("grant_type") grantType: String,
        @Body request: SupabaseAuthRequest
    ): SupabaseAuthResponse

    @GET("rest/v1/posts")
    suspend fun fetchRemotePosts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Query("select") select: String = "*"
    ): List<SupabasePostModel>

    @POST("rest/v1/posts")
    suspend fun upsertPost(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body post: SupabasePostModel
    ): List<SupabasePostModel>
}

// --- Rest Client Singleton ---

object SupabaseClient {
    private const val DEFAULT_URL = "https://cptquthczkjghjwmbojg.supabase.co/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val service: SupabaseService by lazy {
        val baseUrl = try {
            val configUrl = BuildConfig.SUPABASE_URL
            if (configUrl.isNotEmpty() && configUrl.startsWith("http")) {
                if (configUrl.endsWith("/")) configUrl else "$configUrl/"
            } else {
                DEFAULT_URL
            }
        } catch (e: Exception) {
            DEFAULT_URL
        }

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SupabaseService::class.java)
    }

    // Helper checking keys
    fun getApiKey(): String {
        return try {
            val key = BuildConfig.SUPABASE_ANON_KEY
            if (key.isNotEmpty() && !key.startsWith("sb_pub")) key else "sb_publishable_IUsBYEYSv13T-IM2BqtDoQ_va_FKDH8"
        } catch (e: Exception) {
            "sb_publishable_IUsBYEYSv13T-IM2BqtDoQ_va_FKDH8"
        }
    }
}
