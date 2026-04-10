package com.example.gitsage.ai

import com.example.gitsage.settings.AIProviderConfig

interface AIProvider {
    suspend fun generateCommitMessage(
        diff: String,
        config: AIProviderConfig,
        convention: String,
        language: String
    ): String
}

sealed class AIProviderException(message: String) : Exception(message) {
    class NetworkException(message: String) : AIProviderException(message)
    class APIException(val code: Int, message: String) : AIProviderException(message)
    class ValidationException(message: String) : AIProviderException(message)
}
