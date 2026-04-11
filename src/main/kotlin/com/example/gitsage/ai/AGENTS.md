# AI PROVIDER MODULE

**Generated:** 2026-04-12
**Location:** src/main/kotlin/com/example/gitsage/ai/

## OVERVIEW
Contains all AI provider implementations, message generation logic, and HTTP client configuration.

## STRUCTURE
```
ai/
├── AIProvider.kt                    # Abstract base interface for AI providers
├── OpenAICompatibleProvider.kt      # Concrete implementation for OpenAI-compatible APIs
├── CommitMessageGenerator.kt        # Core logic for generating commit messages
├── ModelListFetcher.kt              # Fetches available models from provider API (307 lines)
└── HttpClientConfig.kt              # HTTP client configuration for API requests
```

## WHERE TO LOOK
| Component | File | Purpose |
|-----------|------|---------|
| Provider Interface | AIProvider.kt | Defines contract for all AI providers |
| OpenAI Implementation | OpenAICompatibleProvider.kt | Implements OpenAI-compatible API calls |
| Message Generation | CommitMessageGenerator.kt | Creates prompts and processes AI responses |
| Model Fetching | ModelListFetcher.kt | Retrieves available models from API endpoint |
| HTTP Client | HttpClientConfig.kt | Sets up OkHttp with timeouts and interceptors |

## CONVENTIONS
- Uses OkHttp for HTTP requests with proper timeout configuration
- Implements retry mechanism for API calls
- Supports multiple commit conventions (Conventional, Angular, Emoji, Simple)
- Supports multiple languages (English, Chinese, Auto-detect)

## ANTI-PATTERNS
- No anti-patterns detected in this module