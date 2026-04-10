package com.example.gitsage.ai

import com.example.gitsage.settings.AIProviderConfig
import com.example.gitsage.settings.ProviderType
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenAICompatibleProvider : AIProvider {
    private val logger = Logger.getInstance(OpenAICompatibleProvider::class.java)
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy {
        HttpClientConfig.createClient()
    }

    override suspend fun generateCommitMessage(
        diff: String,
        config: AIProviderConfig,
        convention: String,
        language: String
    ): String {
        if (diff.isBlank()) {
            throw AIProviderException.ValidationException("No changes to generate commit message for")
        }

        val prompt = CommitMessageGenerator.createPrompt(diff, convention, language)

        val requestBody = JsonObject().apply {
            addProperty("model", config.model)
            add("messages", gson.toJsonTree(listOf(
                mapOf("role" to "system", "content" to "You are a helpful assistant that generates concise, meaningful Git commit messages."),
                mapOf("role" to "user", "content" to prompt)
            )))
            addProperty("temperature", config.temperature)
            addProperty("max_tokens", config.maxTokens)
        }.toString()

        val effectiveBaseUrl = config.getEffectiveBaseUrl()
        val effectiveApiKey = config.getEffectiveApiKey()

        val chatUrl = when (config.providerType) {
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> "https://opencode.ai/zen/v1/chat/completions"
            ProviderType.OPENROUTER -> "https://openrouter.ai/api/v1/chat/completions"
            ProviderType.CUSTOM -> "${effectiveBaseUrl.trimEnd('/')}/chat/completions"
        }

        val request = Request.Builder()
            .url(chatUrl)
            .post(requestBody.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $effectiveApiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        logger.debug("Sending request to $effectiveBaseUrl with provider type: ${config.providerType}")

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    logger.error("API error: ${response.code} - $errorBody")
                    throw AIProviderException.APIException(
                        response.code,
                        "API request failed: ${response.message}"
                    )
                }

                val responseBody = response.body?.string()
                    ?: throw AIProviderException.APIException(-1, "Empty response")

                parseResponse(responseBody)
            }
        } catch (e: IOException) {
            logger.error("Network error", e)
            throw AIProviderException.NetworkException("Failed to connect to API: ${e.message}")
        }
    }

    private fun parseResponse(responseBody: String): String {
        return try {
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val choices = json.getAsJsonArray("choices")

            if (choices == null || choices.size() == 0) {
                throw AIProviderException.APIException(-1, "No choices in response")
            }

            val firstChoice = choices[0].asJsonObject
            val message = firstChoice.getAsJsonObject("message")
            val content = message.get("content").asString

            content.trim().trim('"')
        } catch (e: Exception) {
            logger.error("Failed to parse response", e)
            throw AIProviderException.APIException(-1, "Failed to parse API response")
        }
    }
}
