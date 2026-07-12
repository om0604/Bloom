# Bloom — AI Integration

> This document covers the AI reflection feature: design, implementation, error handling, and future extensibility.

---

## Why Groq?

The app originally integrated Google's Gemini SDK (`com.google.ai.client.generativeai`). Despite the API key being valid and models listing successfully, the Android SDK consistently returned "Model not found" errors in this project's Gradle/AGP configuration.

Groq was chosen as the replacement because:
- **OpenAI-compatible REST API** — standard `chat/completions` endpoint, no proprietary SDK required
- **Native SSE streaming** — true token-by-token streaming over HTTP, matching the original Gemini streaming UX
- **Free tier** — generous free usage with no billing required for MVP
- **Speed** — Groq's GroqCloud hardware delivers very low latency inference
- **Simplicity** — OkHttp + Gson is already in the dependency graph; no additional SDK needed

---

## Class: `GeminiService`

**Package:** `com.bloom.app.ai`  
**File:** `GeminiService.kt`

> The class name `GeminiService` is intentionally preserved to minimize architectural churn and Git diff noise. Internally it is 100% Groq-backed.

### Model
```
llama-3.3-70b-versatile
```
Selected for its balance of quality, speed, and token efficiency for short journaling reflections.

### Parameters
| Parameter | Value | Rationale |
|---|---|---|
| `temperature` | 0.85 | Warm, varied — avoids robotic sameness |
| `top_p` | 0.92 | Nucleus sampling for natural prose |
| `max_tokens` | 200 | Forces 3–5 sentence brevity |
| `stream` | `true` | Progressive display as tokens arrive |

---

## Prompt Engineering

### System Instruction
The system prompt defines a **warm, calm journaling companion** — not a therapist, not a life coach.

Key constraints enforced in the prompt:
- Never diagnose emotions
- Never use phrases like "It sounds like you're experiencing..."
- Never start with "I"
- Never mention being an AI
- 3–5 sentences maximum
- No bullet points or headers — flowing prose only
- Write in second person ("you")

### User Message Structure
```
Here's what someone wrote in their journal today:

"[journal content]"

Their mood today: [mood display name]
They were thinking about: "[optional prompt]"

Share a gentle reflection with them.
```

---

## Streaming Implementation

Groq's endpoint returns **Server-Sent Events (SSE)**:
```
data: {"choices":[{"delta":{"content":"You"}}]}
data: {"choices":[{"delta":{"content":" seem"}}]}
data: [DONE]
```

The implementation:
1. Executes a synchronous OkHttp call on `Dispatchers.IO`
2. Reads `response.body.source()` line-by-line using Okio's `readUtf8Line()`
3. Parses each `data: {...}` chunk via Gson
4. Accumulates delta content into a growing string
5. Emits `ReflectionState.Streaming(accumulated)` on every token
6. Breaks on `[DONE]`
7. After flow completes, `JournalViewModel` persists the full text to Room

---

## `ReflectionState`

```kotlin
sealed class ReflectionState {
    data object Loading              : ReflectionState()
    data class Streaming(text: String) : ReflectionState()  // emitted per token
    data class Complete(text: String)  : ReflectionState()  // (future use)
    data class Error(message: String)  : ReflectionState()
}
```

The UI observes this sealed class and renders:
- `Loading` → rotating loading phrases + shimmer
- `Streaming` → live text displayed progressively
- `Error` → error message inline (no crash, no toast)

---

## Error Handling

All errors are caught in the `.catch {}` operator on the flow. User-facing messages are mapped from HTTP status codes and exception types:

| Condition | User Message |
|---|---|
| Blank API key | "Invalid API key: Key is missing." |
| HTTP 401 | "Invalid API key: Please check your API key configuration." |
| HTTP 403 | "Permission denied: Ensure your API key has the right access." |
| HTTP 404 | "Model unavailable: The specified model was not found." |
| HTTP 429 | "Quota exceeded: Please check your Groq API usage limits." |
| SocketTimeoutException | "Network failure: Connection timed out." |
| UnknownHostException / ConnectException | "Network failure: Unable to connect." |
| Other | "An error occurred: [message]" |

Errors always emit `ReflectionState.Error(message)` — the flow never throws to the caller.

---

## Security

- The API key is stored **only in `local.properties`** (git-ignored)
- Injected at build time via `BuildConfig.GROQ_API_KEY`
- Never embedded in source code or committed to version control
- For release builds, the key is compiled in via `buildConfigField` in `app/build.gradle.kts`

---

## Future Provider Abstraction

Currently `GeminiService` is a concrete class. To support multiple providers:

1. Extract an interface:
```kotlin
interface ReflectionService {
    fun generateReflection(
        entryContent: String,
        mood: String,
        prompt: String?
    ): Flow<ReflectionState>
}
```

2. Rename `GeminiService` → `GroqReflectionService : ReflectionService`

3. Add `OpenAIReflectionService`, `GeminiReflectionService`, etc. as alternatives

4. `AppContainer` selects the implementation via config or feature flag

This migration would take approximately 1–2 hours and require no changes to `JournalViewModel` or any UI code.
