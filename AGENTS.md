# PROJECT KNOWLEDGE BASE

**Generated:** 2026-04-12
**Commit:** 03b94b4
**Branch:** main

## OVERVIEW
GitSage - AI-powered Git commit message generator IntelliJ/Android Studio plugin. Written in Kotlin with Gradle build system.

## STRUCTURE
```
gitsage/
├── build.gradle.kts    # Gradle build configuration
├── settings.gradle.kts # Project settings
├── gradle/             # Gradle wrapper and scripts
├── src/                # Source code
│   ├── main/           # Production code
│   │   ├── kotlin/     # Kotlin source files
│   │   └── resources/  # Plugin resources
│   └── test/           # Test code
├── .gitignore          # Git ignore patterns
└── README.md           # Project documentation
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Plugin entry point | src/main/kotlin/com/example/gitsage/AICommitPlugin.kt | Initializes plugin |
| Generate commit action | src/main/kotlin/com/example/gitsage/ui/GenerateCommitAction.kt | UI action handler |
| AI provider logic | src/main/kotlin/com/example/gitsage/ai/ | AI services and models |
| Settings management | src/main/kotlin/com/example/gitsage/settings/ | Configuration handling |
| Git utilities | src/main/kotlin/com/example/gitsage/git/ | Git diff extraction |
| Build configuration | build.gradle.kts | Dependencies and tasks |

## CONVENTIONS
- Kotlin coroutines provided by IntelliJ platform
- Plugin follows IntelliJ Platform SDK guidelines
- Uses OkHttp for HTTP requests
- Uses Gson for JSON processing

## ANTI-PATTERNS (THIS PROJECT)
- No explicit anti-pattern comments found in code
- Naming mismatch: AICommitPlugin.kt file has GitSagePlugin class

## UNIQUE STYLES
- Supports multiple AI providers (OpenAI compatible)
- Multiple commit conventions (Conventional, Angular, Emoji, Simple)
- Multi-language support (English, Chinese, Auto-detect)

## COMMANDS
```bash
./gradlew build          # Build the plugin
./gradlew runIde         # Run plugin in IDE
./gradlew buildPlugin    # Package plugin
```

## NOTES
- Uses Chinese Maven repositories (Aliyun mirrors)
- Single test file for core functionality
- Settings stored using IntelliJ application service