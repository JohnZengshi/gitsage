package com.example.gitsage.ai

import com.example.gitsage.settings.ProviderType
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

data class ModelInfo(
    val id: String,
    val name: String? = null,
    val description: String? = null
)

object ModelListFetcher {
    private val logger = Logger.getInstance(ModelListFetcher::class.java)
    private val gson = Gson()
    private const val OPENCODE_MODELS_URL = "https://opencode.ai/zen/v1/models"

    fun fetchModels(baseUrl: String, apiKey: String, providerType: ProviderType = ProviderType.CUSTOM): List<ModelInfo> {
        logger.info("[ModelListFetcher] Starting fetchModels")
        logger.info("[ModelListFetcher] ProviderType: $providerType")
        logger.info("[ModelListFetcher] BaseUrl: $baseUrl")
        logger.info("[ModelListFetcher] API Key length: ${apiKey.length}")
        logger.info("[ModelListFetcher] API Key first 10 chars: ${apiKey.take(10)}...")
        
        return when (providerType) {
            ProviderType.OPENCODE_ZEN, ProviderType.OPENCODE_GO -> fetchOpenCodeModels(apiKey)
            ProviderType.OPENROUTER -> fetchOpenRouterModels(apiKey)
            ProviderType.CUSTOM -> fetchCustomModels(baseUrl, apiKey)
        }
    }

    private fun fetchOpenCodeModels(apiKey: String): List<ModelInfo> {
        logger.info("[ModelListFetcher] fetchOpenCodeModels called")
        logger.info("[ModelListFetcher] URL: $OPENCODE_MODELS_URL")
        
        val client = HttpClientConfig.createClient()
        logger.info("[ModelListFetcher] HTTP client created")

        val request = Request.Builder()
            .url(OPENCODE_MODELS_URL)
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        logger.info("[ModelListFetcher] HTTP request built")

        return try {
            logger.info("[ModelListFetcher] Executing HTTP request...")
            client.newCall(request).execute().use { response ->
                logger.info("[ModelListFetcher] Response received")
                logger.info("[ModelListFetcher] Response code: ${response.code}")
                logger.info("[ModelListFetcher] Response message: ${response.message}")
                logger.info("[ModelListFetcher] Response headers: ${response.headers}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No response body"
                    logger.error("[ModelListFetcher] Failed to fetch OpenCode models: ${response.code}")
                    logger.error("[ModelListFetcher] Error body: $errorBody")
                    throw AIProviderException.APIException(
                        response.code,
                        "HTTP ${response.code}: ${response.message}\n\nResponse body:\n$errorBody"
                    )
                }

                val responseBody = response.body?.string()
                    ?: throw AIProviderException.APIException(-1, "Empty response body")
                
                logger.info("[ModelListFetcher] Response body length: ${responseBody.length}")
                logger.info("[ModelListFetcher] Response body preview: ${responseBody.take(500)}")

                parseModels(responseBody)
            }
        } catch (e: IOException) {
            logger.error("[ModelListFetcher] Network error fetching OpenCode models", e)
            val errorDetail = buildString {
                appendLine("Network error: ${e.message}")
                appendLine()
                appendLine("URL: $OPENCODE_MODELS_URL")
                appendLine()
                appendLine("Stack trace:")
                e.stackTrace.take(10).forEach { appendLine(it.toString()) }
            }
            throw AIProviderException.NetworkException(errorDetail)
        }
    }

    private fun fetchCustomModels(baseUrl: String, apiKey: String): List<ModelInfo> {
        val client = HttpClientConfig.createClient()
        val normalizedUrl = baseUrl.trimEnd('/')

        val request = Request.Builder()
            .url("$normalizedUrl/models")
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error("Failed to fetch models: ${response.code}")
                    throw AIProviderException.APIException(
                        response.code,
                        "Failed to fetch models: ${response.message}"
                    )
                }

                val responseBody = response.body?.string()
                    ?: throw AIProviderException.APIException(-1, "Empty response")

                parseModels(responseBody)
            }
        } catch (e: IOException) {
            logger.error("Network error fetching models", e)
            throw AIProviderException.NetworkException("Failed to fetch models: ${e.message}")
        }
    }

    private fun fetchOpenRouterModels(apiKey: String): List<ModelInfo> {
        val client = HttpClientConfig.createClient()
        val url = "https://openrouter.ai/api/v1/models"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error("Failed to fetch OpenRouter models: ${response.code}")
                    throw AIProviderException.APIException(
                        response.code,
                        "Failed to fetch models: ${response.message}"
                    )
                }

                val responseBody = response.body?.string()
                    ?: throw AIProviderException.APIException(-1, "Empty response")

                parseModels(responseBody)
            }
        } catch (e: IOException) {
            logger.error("Network error fetching OpenRouter models", e)
            throw AIProviderException.NetworkException("Failed to fetch models: ${e.message}")
        }
    }

    fun testConnection(
        baseUrl: String,
        apiKey: String,
        providerType: ProviderType = ProviderType.CUSTOM,
        model: String? = null
    ): Pair<Boolean, String> {
        logger.info("[ModelListFetcher] testConnection called")
        logger.info("[ModelListFetcher] ProviderType: $providerType")
        logger.info("[ModelListFetcher] BaseUrl: $baseUrl")
        logger.info("[ModelListFetcher] Model: $model")

        return if (model.isNullOrBlank()) {
            logger.info("[ModelListFetcher] No model specified, fetching model list")
            try {
                val models = fetchModels(baseUrl, apiKey, providerType)
                if (models.isNotEmpty()) {
                    Pair(true, "Connection successful! Found ${models.size} models.")
                } else {
                    Pair(false, "Connection successful but no models found.")
                }
            } catch (e: AIProviderException.NetworkException) {
                throw e
            } catch (e: AIProviderException.APIException) {
                throw e
            } catch (e: Exception) {
                throw e
            }
        } else {
            logger.info("[ModelListFetcher] Testing specific model: $model")
            testModelCompletion(baseUrl, apiKey, providerType, model)
        }
    }

    private fun testModelCompletion(
        baseUrl: String,
        apiKey: String,
        providerType: ProviderType,
        model: String
    ): Pair<Boolean, String> {
        val client = HttpClientConfig.createClient()

        val chatUrl = when (providerType) {
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> "https://opencode.ai/zen/v1/chat/completions"
            ProviderType.OPENROUTER -> "https://openrouter.ai/api/v1/chat/completions"
            ProviderType.CUSTOM -> "${baseUrl.trimEnd('/')}/chat/completions"
        }

        val requestBody = buildString {
            append("{")
            append("\"model\": \"$model\",")
            append("\"messages\": [{\"role\": \"user\", \"content\": \"Hi\"}],")
            append("\"max_tokens\": 5")
            append("}")
        }

        logger.info("[ModelListFetcher] Testing model completion at: $chatUrl")

        val request = Request.Builder()
            .url(chatUrl)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                logger.info("[ModelListFetcher] Test response code: ${response.code}")

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No response body"
                    logger.error("[ModelListFetcher] Model test failed: ${response.code}")
                    logger.error("[ModelListFetcher] Error body: $errorBody")

                    val errorMessage = try {
                        val json = gson.fromJson(errorBody, JsonObject::class.java)
                        json.getAsJsonObject("error")?.get("message")?.asString
                            ?: "HTTP ${response.code}: ${response.message}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}: ${response.message}\n\n$errorBody"
                    }

                    throw AIProviderException.APIException(response.code, errorMessage)
                }

                val responseBody = response.body?.string()
                    ?: throw AIProviderException.APIException(-1, "Empty response body")

                logger.info("[ModelListFetcher] Model test successful")
                logger.info("[ModelListFetcher] Response preview: ${responseBody.take(200)}")

                Pair(true, "Model '$model' is working! Connection test successful.")
            }
        } catch (e: IOException) {
            logger.error("[ModelListFetcher] Network error testing model", e)
            throw AIProviderException.NetworkException("Failed to test model: ${e.message}")
        }
    }

    private fun parseModels(responseBody: String): List<ModelInfo> {
        logger.info("[ModelListFetcher] Parsing models response")
        return try {
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            logger.info("[ModelListFetcher] JSON parsed successfully")
            logger.info("[ModelListFetcher] JSON keys: ${json.keySet()}")
            
            val data = when {
                json.has("data") -> {
                    logger.info("[ModelListFetcher] Found 'data' array")
                    json.getAsJsonArray("data")
                }
                json.has("models") -> {
                    logger.info("[ModelListFetcher] Found 'models' array")
                    json.getAsJsonArray("models")
                }
                else -> {
                    logger.warn("[ModelListFetcher] No 'data' or 'models' array found in response")
                    null
                }
            } ?: return emptyList()

            logger.info("[ModelListFetcher] Data array size: ${data.size()}")

            val models = data.mapNotNull { element ->
                try {
                    val obj = element.asJsonObject
                    val id = when {
                        obj.has("id") -> obj.get("id")?.asString
                        obj.has("name") -> obj.get("name")?.asString
                        else -> null
                    } ?: return@mapNotNull null

                    ModelInfo(
                        id = id,
                        name = obj.get("name")?.asString,
                        description = obj.get("description")?.asString
                    )
                } catch (e: Exception) {
                    logger.warn("[ModelListFetcher] Failed to parse model element", e)
                    null
                }
            }
            
            logger.info("[ModelListFetcher] Successfully parsed ${models.size} models")
            models
        } catch (e: Exception) {
            logger.error("[ModelListFetcher] Failed to parse models response", e)
            emptyList()
        }
    }
}
