package com.bloom.app.ai

import com.bloom.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import android.util.Log
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.ConnectException

// ─────────────────────────────────────────────────────────────────────────────
// GeminiService
//
// Wraps the Gemini API for gentle, reflective journaling insights.
//
// Design constraints (from product brief):
//   - NEVER behave like a therapist
//   - NEVER diagnose or suggest medical conditions
//   - NEVER use clinical language
//   - NEVER be overly positive or "toxic positivity"
//   - Sound like a warm, thoughtful friend who listened carefully
//   - Keep responses short — 3 to 5 sentences
//   - Streaming is used so the text feels alive as it appears
//
// The system prompt is the most important part of this service.
// It is crafted to produce responses that feel human and warm,
// not like an AI reading a mood analysis report.
// ─────────────────────────────────────────────────────────────────────────────

sealed class ReflectionState {
    data object Loading : ReflectionState()
    data class Streaming(val text: String) : ReflectionState()
    data class Complete(val text: String) : ReflectionState()
    data class Error(val message: String) : ReflectionState()
}

class GeminiService {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",     // Using -latest alias to fix 404 Model Not Found
        apiKey    = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature         = 0.85f     // Slightly creative, not robotic
            topP                = 0.92f
            maxOutputTokens     = 200       // ~3-5 sentences — intentionally brief
            candidateCount      = 1
        },
        systemInstruction = buildSystemInstruction(),
    )

    /**
     * Generate a reflection for a journal entry.
     * Returns a Flow that emits streaming text chunks as they arrive.
     *
     * @param entryContent  The user's journal text
     * @param mood          The mood they selected (display name, e.g. "Good")
     * @param prompt        The journal prompt used, if any
     */
    fun generateReflection(
        entryContent : String,
        mood         : String,
        prompt       : String? = null,
    ): Flow<ReflectionState> {
        val userMessage = buildUserMessage(entryContent, mood, prompt)
        
        Log.d("GeminiService", "SDK version: 0.9.0")
        Log.d("GeminiService", "model name: gemini-1.5-flash-latest")
        Log.d("GeminiService", "endpoint: https://generativelanguage.googleapis.com/v1beta")
        Log.d("GeminiService", "API key present: ${BuildConfig.GEMINI_API_KEY.isNotBlank()}")
        Log.d("GeminiService", "prompt length: ${userMessage.length}")
        
        var accumulated = ""

        return model.generateContentStream(userMessage)
            .map { chunk ->
                accumulated += chunk.text ?: ""
                ReflectionState.Streaming(accumulated) as ReflectionState
            }
            .catch { error ->
                Log.e("GeminiService", "Generation failed. Full exception: ${error.message}", error)
                Log.e("GeminiService", "HTTP/API Error Body (if present in trace): ${error.cause?.message ?: error.toString()}")
                
                val errorMessage = error.message?.lowercase() ?: ""
                
                val userFriendlyMessage = when {
                    BuildConfig.GEMINI_API_KEY.isBlank() -> 
                        "Invalid API key: Key is missing."
                    errorMessage.contains("api key not valid") || errorMessage.contains("api_key_invalid") || errorMessage.contains("invalid api key") -> 
                        "Invalid API key: Please check your API key configuration."
                    errorMessage.contains("403") || errorMessage.contains("permission denied") -> 
                        "Permission denied: Ensure your API key has the right access."
                    errorMessage.contains("404") || errorMessage.contains("not found") -> 
                        "Model unavailable: The specified model was not found."
                    errorMessage.contains("429") || errorMessage.contains("quota") || errorMessage.contains("rate limit") -> 
                        "Quota exceeded: Please check your Gemini API usage limits."
                    error is java.net.SocketTimeoutException || errorMessage.contains("timeout") -> 
                        "Network failure: Connection timed out."
                    error is java.net.UnknownHostException || error is java.net.ConnectException || errorMessage.contains("network") -> 
                        "Network failure: Unable to connect to Gemini."
                    else -> 
                        "An error occurred: ${error.message}"
                }
                emit(ReflectionState.Error(userFriendlyMessage))
            }
    }

    /**
     * Builds the user message sent to Gemini.
     * Structured but conversational — not a formal prompt template.
     */
    private fun buildUserMessage(
        content : String,
        mood    : String,
        prompt  : String?,
    ): String = buildString {
        append("Here's what someone wrote in their journal today:\n\n")
        append("\"$content\"\n\n")
        append("Their mood today: $mood\n")
        if (!prompt.isNullOrBlank()) {
            append("They were thinking about: \"$prompt\"\n")
        }
        append("\nShare a gentle reflection with them.")
    }

    /**
     * The system instruction shapes every response Gemini gives.
     * This is the single most impactful tuning we do.
     */
    private fun buildSystemInstruction() =
        content(role = "system") {
            text("""
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
            """.trimIndent())
        }
}
