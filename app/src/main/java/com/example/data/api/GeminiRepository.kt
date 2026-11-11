package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

sealed class ApiConnectionStatus {
    object Untested : ApiConnectionStatus()
    data class Connected(val latencyMs: Long, val message: String) : ApiConnectionStatus()
    data class Failed(val error: String, val isQuotaError: Boolean = false) : ApiConnectionStatus()
}

data class ConnectionTestResult(
    val success: Boolean,
    val latencyMs: Long,
    val message: String
)

class GeminiRepository {

    // IN-MEMORY KEY STORAGE ONLY:
    // Strictly in memory for session, never persisted to disk/prefs per safety requirements.
    private var _inMemoryKey: String? = null

    var inMemoryKey: String?
        get() = _inMemoryKey
        set(value) {
            _inMemoryKey = value?.trim()
        }

    fun getEffectiveKey(): String {
        return _inMemoryKey?.takeIf { it.isNotBlank() }
            ?: (try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" })
                .takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            ?: ""
    }

    fun clearKey() {
        _inMemoryKey = null
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    companion object {
        val SYSTEM_INSTRUCTION = """
You are INJAHNIA, an expert precision automotive engine machining and reconditioning specialist AI assistant.
Your mission is to provide accurate, workshop-ready guidance for machinists, engine builders, and reconditioning workshops.

PRECISION SPECIFICATION & TOLERANCE FORMATTING RULES:
1. Whenever providing engine dimensions, clearances, or tolerances, ALWAYS format them in clean, structured Markdown Tables so the app can render them into high-precision workshop spec cards:
| Parameter | Minimum | Maximum | Target / Nominal | Wear Limit | Unit | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
Always provide both metric (mm) and imperial (inch) where helpful for engine builders.

2. For torque specifications, ALWAYS structure them as a step-by-step sequence:
### TORQUE SPECIFICATION: [Component Name, e.g. Cylinder Head Bolts]
- Step 1: [Torque value in Nm and ft-lb, e.g. 35 Nm (26 ft-lb)] - [Procedure notes]
- Step 2: [Torque value or angle, e.g. +90° TTY] - [Torque-to-yield turn]
- Step 3: [Final torque or angle, e.g. +90° TTY] - [Final yield turn in spiral sequence]

3. For critical clearances, always include:
• Piston-to-cylinder wall clearance (specify cast vs forged alloy 4032 vs 2618)
• Crankshaft main bearing oil clearance
• Connecting rod bearing oil clearance
• Piston ring end gaps (Top ring, 2nd ring, Oil rail)
• Valve guide-to-stem clearance (Intake & Exhaust)
• Deck surface roughness average (Ra < 30 µin for MLS gaskets)

4. Always categorize your specifications clearly using these labels:
   - [VERIFIED OEM SPEC] - Direct OEM factory manual specification.
   - [PARTIALLY VERIFIED] - High-reputation aftermarket/component manufacturer standard.
   - [GENERAL WORKSHOP GUIDELINE] - Universal reconditioning standard practice.
   - [UNVERIFIED] - Unconfirmed spec that MUST be verified before cutting metal.

5. Provide precise calculations for oversize and undersize operations:
   - Cylinder bore & hone: Nominal bore + Oversize (e.g., +0.50mm / +0.020") with piston skirt clearance.
   - Crankshaft journal grinding: Standard journal - Undersize (-0.25mm / -0.010", -0.50mm / -0.020") with bearing oil clearance calculations.
6. Valve train precision:
   - Strictly distinguish between: Valve Protrusion (above deck), Valve Recession (below deck), Valve Stem Installed Height, and Seat Cut Depth.
   - Explain multi-angle valve seat cutting (typically 30° top cut, 45° seating face, 60°/75° throat bottom cut) and seat width limits (1.2-1.5mm intake, 1.5-1.8mm exhaust).
7. Flag conflicting specifications immediately and explain the engineering difference rather than guessing.
8. Surface finish: Always specify required Ra/Rz (e.g. Ra < 30 for MLS gaskets, Ra < 60 for composite) and cylinder wall crosshatch hone angles (30°-45°).
        """.trimIndent()
    }

    suspend fun testConnection(apiKey: String): Result<ConnectionTestResult> = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key is empty. Please enter your Gemini API key."))
        }

        val startTime = System.currentTimeMillis()
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Hello INJAHNIA. Confirm engine machining assistant readiness in one short sentence."))
                )
            ),
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = "You are INJAHNIA automotive machining assistant. Acknowledge with a single short greeting."))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.1f)
        )

        try {
            val response = apiService.generateContent(key, request)
            val duration = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    Result.success(ConnectionTestResult(true, duration, "Connected successfully: ${text.trim()}"))
                } else {
                    Result.failure(Exception("Received empty response from Gemini API."))
                }
            } else {
                val code = response.code()
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                val message = when (code) {
                    400 -> "Invalid API Key or Bad Request ($code). Verify your key at aistudio.google.com"
                    403 -> "Permission Denied ($code). The API key lacks permissions or Gemini API is not enabled."
                    429 -> "Quota Exceeded ($code). Free tier rate limit reached. Please wait or check billing."
                    404 -> "Model not found ($code). Check model availability."
                    500, 503 -> "Gemini API server temporary error ($code). Try again shortly."
                    else -> "API Error ($code): $errorBody"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            val errMsg = when {
                e is java.net.UnknownHostException -> "No network connection. Check internet access."
                e is java.net.SocketTimeoutException -> "Connection timed out after ${duration}ms. Check network."
                else -> e.localizedMessage ?: "Connection error: ${e.javaClass.simpleName}"
            }
            Result.failure(Exception(errMsg))
        }
    }

    suspend fun queryMachiningAssistant(
        prompt: String,
        engineContext: String? = null,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = getEffectiveKey()
        if (key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("No Gemini API key supplied. Please configure your key in Settings.")
            )
        }

        val contents = mutableListOf<GeminiContent>()

        // Add conversation history
        for ((userMsg, assistantMsg) in conversationHistory) {
            contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMsg))))
            contents.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = assistantMsg))))
        }

        // Add current prompt enriched with engine context if present
        val enrichedPrompt = if (!engineContext.isNullOrBlank()) {
            "ENGINE FOCUS / SPEC CONTEXT: $engineContext\n\nUSER QUERY: $prompt"
        } else {
            prompt
        }
        contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = enrichedPrompt))))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = SYSTEM_INSTRUCTION))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f)
        )

        try {
            val response = apiService.generateContent(key, request)
            if (response.isSuccessful) {
                val candidateText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!candidateText.isNullOrBlank()) {
                    Result.success(candidateText)
                } else {
                    Result.failure(Exception("No answer text generated by model."))
                }
            } else {
                val code = response.code()
                val err = response.errorBody()?.string() ?: ""
                val friendlyMsg = when (code) {
                    400, 403 -> "Invalid or restricted Gemini API key. Check Settings."
                    429 -> "Gemini API free tier quota limit reached. Please wait a minute or upgrade quota."
                    else -> "Gemini API error ($code): $err"
                }
                Result.failure(Exception(friendlyMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
