package com.example.gitsage.ai

import com.example.gitsage.settings.CommitConvention
import com.example.gitsage.settings.GenerationLanguage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CommitMessageGeneratorTest {

    @Test
    fun testCreatePromptWithConventionalCommits() {
        val diff = """
            + Added new login functionality
            + Fixed password validation
        """.trimIndent()

        val prompt = CommitMessageGenerator.createPrompt(
            diff = diff,
            convention = "CONVENTIONAL_COMMITS",
            language = "ENGLISH"
        )

        assertTrue(prompt.contains("Conventional Commits"))
        assertTrue(prompt.contains("English"))
        assertTrue(prompt.contains(diff))
        assertTrue(prompt.contains("Git diff"))
    }

    @Test
    fun testCreatePromptWithAngular() {
        val diff = "Some changes"

        val prompt = CommitMessageGenerator.createPrompt(
            diff = diff,
            convention = "ANGULAR",
            language = "CHINESE"
        )

        assertTrue(prompt.contains("Angular"))
        assertTrue(prompt.contains("Chinese"))
    }

    @Test
    fun testCreatePromptWithAutoLanguage() {
        val diff = "Changes"

        val prompt = CommitMessageGenerator.createPrompt(
            diff = diff,
            convention = "SIMPLE",
            language = "AUTO"
        )

        assertTrue(prompt.contains("appropriate language"))
    }

    @Test
    fun testGetConventionDescription() {
        val conventionalCommits = CommitMessageGenerator.getConventionDescription(CommitConvention.CONVENTIONAL_COMMITS)
        assertTrue(conventionalCommits.contains("type(scope)"))

        val angular = CommitMessageGenerator.getConventionDescription(CommitConvention.ANGULAR)
        assertTrue(angular.contains("Angular"))

        val emoji = CommitMessageGenerator.getConventionDescription(CommitConvention.EMOJI)
        assertTrue(emoji.contains("Emoji"))

        val simple = CommitMessageGenerator.getConventionDescription(CommitConvention.SIMPLE)
        assertTrue(simple.contains("Simple"))
    }
}
