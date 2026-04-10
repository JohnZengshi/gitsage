package com.example.gitsage.ui

import com.example.gitsage.ai.AIProviderException
import com.example.gitsage.ai.OpenAICompatibleProvider
import com.example.gitsage.git.GitDiffExtractor
import com.example.gitsage.notifications.NotificationHelper
import com.example.gitsage.settings.GitSageSettings
import com.example.gitsage.settings.CredentialsManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GenerateCommitAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = GitSageSettings.getInstance()

        val provider = settings.getSelectedProvider()
        if (provider == null) {
            NotificationHelper.showError(project, "No AI provider configured")
            return
        }

        val apiKey = CredentialsManager.getApiKey(provider.id)
        if (apiKey.isNullOrEmpty()) {
            NotificationHelper.showError(project, "API key not configured for ${provider.name}")
            return
        }

        provider.apiKey = apiKey

        val diffExtractor = GitDiffExtractor(project)
        val (changes, unversionedFiles) = getSelectedChanges(e, diffExtractor)

        if (changes.isEmpty() && unversionedFiles.isEmpty()) {
            NotificationHelper.showWarning(project, "No changes selected")
            return
        }

        val diff = diffExtractor.buildDiff(changes, unversionedFiles)
        if (diff.isBlank()) {
            NotificationHelper.showWarning(project, "No diff content available")
            return
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Generating commit message...",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Analyzing changes with AI..."

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val aiProvider = OpenAICompatibleProvider()
                        val commitMessage = aiProvider.generateCommitMessage(
                            diff = diff,
                            config = provider,
                            convention = settings.state.convention.name,
                            language = settings.state.language.name
                        )

                        withContext(Dispatchers.Main) {
                            setCommitMessage(e, commitMessage)
                            NotificationHelper.showInfo(project, "Commit message generated successfully")
                        }
                    } catch (e: AIProviderException.NetworkException) {
                        withContext(Dispatchers.Main) {
                            NotificationHelper.showError(project, "Network error: ${e.message}")
                        }
                    } catch (e: AIProviderException.APIException) {
                        withContext(Dispatchers.Main) {
                            NotificationHelper.showError(project, "API error: ${e.message}")
                        }
                    } catch (e: AIProviderException.ValidationException) {
                        withContext(Dispatchers.Main) {
                            NotificationHelper.showError(project, "Validation error: ${e.message}")
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            NotificationHelper.showError(project, "Unexpected error: ${e.message}")
                        }
                    }
                }
            }
        })
    }

    private fun getSelectedChanges(e: AnActionEvent, diffExtractor: GitDiffExtractor): Pair<List<Change>, List<com.intellij.openapi.vcs.FilePath>> {
        val explicitSelection = e.getData(VcsDataKeys.CHANGES)
        
        if (explicitSelection != null && explicitSelection.isNotEmpty()) {
            return Pair(explicitSelection.toList(), emptyList())
        }

        return diffExtractor.getAllChanges()
    }

    private fun setCommitMessage(e: AnActionEvent, message: String) {
        val commitMessageI = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
        commitMessageI?.setCommitMessage(message)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}
