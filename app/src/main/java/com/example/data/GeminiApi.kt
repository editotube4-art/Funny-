package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String?
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }

    suspend fun getMemeAICaption(memeTopic: String): String {
        return try {
            val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return "Simulated AI: Why did the code crash? Because it didn't have its morning coffee! #coding #programming #meme #funny"
            }

            val prompt = "Generate a funny, viral meme caption with hashtags for the topic: \"$memeTopic\". Make it sound witty and ready to trend on social media! Keep it under 200 characters, including 3-4 trending hashtags."
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )

            val apiRes = service.generateContent(apiKey, request)
            apiRes.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Generated caption: 😂 Life is too short to write bugs manually! #programming #funny"
        } catch (e: Exception) {
            "Fallback AI: When life gives you errors, turn them into features! #devlife #funny #memes"
        }
    }
}
