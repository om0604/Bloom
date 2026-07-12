package com.bloom.app.ai

import android.util.Log
import com.bloom.app.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────────────
// GeminiService (Now backed by Groq REST API)
//
// Wraps the Groq API for gentle, reflective journaling insights.
// (Class intentionally not renamed to minimize architectural changes)
// ─────────────────────────────────────────────────────────────────────────────

sealed class ReflectionState {
    data object Loading : ReflectionState()
    data class Streaming(val text: String) : ReflectionState()
    data class Complete(val text: String) : ReflectionState()
    data class Error(val message: String) : ReflectionState()
}

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun generateReflection(
        entryContent: String,
        mood: String,
        prompt: String? = null,
    ): Flow<ReflectionState> = flow {
        val userMessage = buildUserMessage(entryContent, mood, prompt)
        val systemMessage = buildSystemInstruction()
        
        val jsonBody = JsonObject().apply {
            addProperty("model", "llama-3.3-70b-versatile")
            addProperty("temperature", 0.85)
            addProperty("top_p", 0.92)
            addProperty("max_tokens", 200)
            addProperty("stream", true)
            
            val messagesArray = com.google.gson.JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", systemMessage)
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", userMessage)
                })
            }
            add("messages", messagesArray)
        }

        val requestBody = jsonBody.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .post(requestBody)
            .build()

        var accumulated = ""

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP ${response.code}: ${response.body?.string()}")
            }

            response.body?.source()?.let { source ->
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ").trim()
                        if (data == "[DONE]") break
                        
                        try {
                            val chunk = gson.fromJson(data, JsonObject::class.java)
                            val choices = chunk.getAsJsonArray("choices")
                            if (choices != null && choices.size() > 0) {
                                val delta = choices.get(0).asJsonObject.getAsJsonObject("delta")
                                if (delta != null && delta.has("content")) {
                                    val text = delta.get("content").asString
                                    accumulated += text
                                    emit(ReflectionState.Streaming(accumulated) as ReflectionState)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("GeminiService", "Failed to parse chunk: $data", e)
                        }
                    }
                }
            }
        } catch (error: Exception) {
            throw error
        }
    }
    .catch { error ->
        Log.e("GeminiService", "Generation failed. Full exception: ${error.message}", error)
        val errorMessage = error.message?.lowercase() ?: ""
        
        val userFriendlyMessage = when {
            BuildConfig.GROQ_API_KEY.isBlank() -> 
                "Invalid API key: Key is missing."
            errorMessage.contains("http 401") || errorMessage.contains("unauthorized") -> 
                "Invalid API key: Please check your API key configuration."
            errorMessage.contains("http 403") || errorMessage.contains("permission denied") -> 
                "Permission denied: Ensure your API key has the right access."
            errorMessage.contains("http 404") || errorMessage.contains("not found") -> 
                "Model unavailable: The specified model was not found."
            errorMessage.contains("http 429") || errorMessage.contains("quota") || errorMessage.contains("rate limit") -> 
                "Quota exceeded: Please check your Groq API usage limits."
            error is SocketTimeoutException || errorMessage.contains("timeout") -> 
                "Network failure: Connection timed out."
            error is UnknownHostException || error is ConnectException || errorMessage.contains("network") -> 
                "Network failure: Unable to connect."
            else -> 
                "An error occurred: ${error.message}"
        }
        emit(ReflectionState.Error(userFriendlyMessage))
    }
    .flowOn(Dispatchers.IO)

    private fun buildUserMessage(
        content: String,
        mood: String,
        prompt: String?,
    ): String = buildString {
        append("Here's what someone wrote in their journal today:\n\n")
        append("\"$content\"\n\n")
        append("Their mood today: $mood\n")
        if (!prompt.isNullOrBlank()) {
            append("They were thinking about: \"$prompt\"\n")
        }
        append("\nShare a gentle reflection with them.")
    }

    private fun buildSystemInstruction(): String = """
        You are a warm, thoughtful companion helping someone reflect on their day through journaling.
        
        Your role:
        - Listen carefully to what they shared
        - Offer a gentle, human observation — not advice
        - Help them see something they might not have noticed
        - Validate their feelings without amplifying distress
        - End with something encouraging but honest
        
        Your voice:
        - Warm, calm, like a trusted friend who listens well
        - Never clinical, never like a therapist or counselor
        - Never diagnose emotions or suggest mental health conditions
        - Never use phrases like "It sounds like you're experiencing..."
        - Never be excessively positive — that feels hollow
        - Honest, real, grounded
        
        Format:
        - 3 to 5 sentences maximum
        - No bullet points, no headers, no lists
        - Flowing, conversational prose
        - Write in second person ("you", not "they")
        - Never start with "I" — start with an observation about them
        - Never mention being an AI
    """.trimIndent()
}
