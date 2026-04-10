package com.example.gitsage.settings

data class AIProviderConfig(
    var id: String = "",
    var name: String = "",
    var providerType: ProviderType = ProviderType.CUSTOM,
    var baseUrl: String = "https://api.openai.com/v1",
    var apiKey: String = "",
    var model: String = "gpt-3.5-turbo",
    var temperature: Double = 0.7,
    var maxTokens: Int = 500,
    var cachedModels: MutableList<String> = mutableListOf()
) {
    fun getEffectiveBaseUrl(): String {
        return when (providerType) {
            ProviderType.OPENCODE_GO, ProviderType.OPENCODE_ZEN -> "https://opencode.ai"
            ProviderType.OPENROUTER -> "https://openrouter.ai/api/v1"
            ProviderType.CUSTOM -> baseUrl
        }
    }

    fun getEffectiveApiKey(): String {
        return apiKey
    }

    fun requiresApiKey(): Boolean {
        return true
    }

    fun isOpenCode(): Boolean {
        return providerType == ProviderType.OPENCODE_GO || providerType == ProviderType.OPENCODE_ZEN
    }
}

enum class ProviderType {
    CUSTOM,
    OPENCODE_GO,
    OPENCODE_ZEN,
    OPENROUTER
}

enum class CommitConvention {
    CONVENTIONAL_COMMITS,
    ANGULAR,
    EMOJI,
    SIMPLE
}

enum class GenerationLanguage {
    ENGLISH,
    CHINESE,
    AUTO
}

data class GitSageSettingsState(
    var providers: MutableList<AIProviderConfig> = mutableListOf(
        AIProviderConfig(
            id = "openai",
            name = "OpenAI",
            providerType = ProviderType.CUSTOM,
            baseUrl = "https://api.openai.com/v1",
            model = "gpt-3.5-turbo"
        ),
        AIProviderConfig(
            id = "opencode",
            name = "OpenCode",
            providerType = ProviderType.OPENCODE_GO,
            baseUrl = "https://opencode.ai",
            model = "gpt-3.5-turbo"
        ),
        AIProviderConfig(
            id = "openrouter",
            name = "OpenRouter",
            providerType = ProviderType.OPENROUTER,
            baseUrl = "https://openrouter.ai/api/v1",
            model = "anthropic/claude-3.5-sonnet"
        )
    ),
    var selectedProviderId: String = "openai",
    var convention: CommitConvention = CommitConvention.CONVENTIONAL_COMMITS,
    var language: GenerationLanguage = GenerationLanguage.AUTO
)
