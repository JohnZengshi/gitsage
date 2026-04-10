package com.example.gitsage.ai

import com.example.gitsage.settings.CommitConvention

object CommitMessageGenerator {

    fun createPrompt(diff: String, convention: String, language: String): String {
        val conventionDescription = when (convention.uppercase()) {
            "CONVENTIONAL_COMMITS" -> "Conventional Commits (type(scope): description)"
            "ANGULAR" -> "Angular style (type(scope): subject)"
            "EMOJI" -> "Emoji style (✨ description)"
            else -> "Simple style (short description)"
        }

        val languageInstruction = when (language.uppercase()) {
            "ENGLISH" -> "Generate the commit message in English."
            "CHINESE" -> "Generate the commit message in Chinese."
            else -> "Generate the commit message in the most appropriate language based on the code changes."
        }

        return """
            Analyze the following Git diff and generate a concise, meaningful commit message.
            
            Convention: $conventionDescription
            $languageInstruction
            
            Requirements:
            - Keep the message concise (under 72 characters for the first line if possible)
            - Use imperative mood (e.g., "Add feature" not "Added feature")
            - Be specific about what changed
            - Focus on the "why" and "what", not just the "how"
            
            Git diff:
            ```
            $diff
            ```
            
            Generate only the commit message, no explanations or additional text.
        """.trimIndent()
    }

    fun getConventionDescription(convention: CommitConvention): String = when (convention) {
        CommitConvention.CONVENTIONAL_COMMITS -> {
            """
            Conventional Commits format:
            type(scope): description
            
            Types: feat, fix, docs, style, refactor, test, chore
            Example: feat(auth): add OAuth2 login support
            """
        }
        CommitConvention.ANGULAR -> {
            """
            Angular commit format:
            type(scope): subject
            
            Types: feat, fix, docs, style, refactor, test, chore
            Example: fix(api): resolve null pointer exception
            """
        }
        CommitConvention.EMOJI -> {
            """
            Emoji commit format:
            ✨ description
            
            Use appropriate emojis for the change type
            Example: 🐛 Fix memory leak in data processing
            """
        }
        CommitConvention.SIMPLE -> {
            """
            Simple commit format:
            Short, clear description
            
            Example: Update README with installation instructions
            """
        }
    }
}
