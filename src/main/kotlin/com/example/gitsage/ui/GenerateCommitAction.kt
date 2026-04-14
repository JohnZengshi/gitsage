package com.example.gitsage.ui

import com.example.gitsage.ai.AIProviderException
import com.example.gitsage.ai.OpenAICompatibleProvider
import com.example.gitsage.git.GitDiffExtractor
import com.example.gitsage.notifications.NotificationHelper
import com.example.gitsage.settings.CredentialsManager
import com.example.gitsage.settings.GitSageSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JComponent
import javax.swing.text.JTextComponent

class GenerateCommitAction : AnAction() {
    private val logger = Logger.getInstance(GenerateCommitAction::class.java)

    companion object {
        private const val ACTION_TEXT_DEFAULT = "Generate Commit Message"
        private const val ACTION_TEXT_LOADING = "Generating..."
        private val generatingProjects = ConcurrentHashMap<String, Boolean>()
    }

    private data class CommitInputState(
        val control: Any?,
        val originalMessage: String
    )

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        if (isGenerating(project)) return

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

        setGeneratingState(project, true)

        CoroutineScope(Dispatchers.IO).launch {
            var commitInputState: CommitInputState? = null
            var generatedMessage: String? = null

            try {
                withContext(Dispatchers.Main) {
                    commitInputState = setCommitInputGeneratingState(e)
                }

                val aiProvider = OpenAICompatibleProvider()
                generatedMessage = aiProvider.generateCommitMessage(
                    diff = diff,
                    config = provider,
                    convention = settings.state.convention.name,
                    language = settings.state.language.name
                )

                withContext(Dispatchers.Main) {
                    NotificationHelper.showInfo(project, "Commit message generated successfully")
                }
            } catch (ex: AIProviderException.NetworkException) {
                withContext(Dispatchers.Main) {
                    logger.error("Commit generation network error", ex)
                    NotificationHelper.showDetailedError(project, "Network Error", buildUserErrorDetail(ex))
                }
            } catch (ex: AIProviderException.APIException) {
                withContext(Dispatchers.Main) {
                    logger.error("Commit generation API error", ex)
                    NotificationHelper.showDetailedError(project, "API Error", buildUserErrorDetail(ex))
                }
            } catch (ex: AIProviderException.ValidationException) {
                withContext(Dispatchers.Main) {
                    logger.error("Commit generation validation error", ex)
                    NotificationHelper.showDetailedError(project, "Validation Error", buildUserErrorDetail(ex))
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    logger.error("Commit generation unexpected error", ex)
                    NotificationHelper.showDetailedError(project, "Unexpected Error", buildUserErrorDetail(ex))
                }
            } finally {
                withContext(Dispatchers.Main) {
                    restoreCommitInputState(e, commitInputState, generatedMessage)
                    setGeneratingState(project, false)
                }
            }
        }
    }

    private fun getSelectedChanges(e: AnActionEvent, diffExtractor: GitDiffExtractor): Pair<List<Change>, List<com.intellij.openapi.vcs.FilePath>> {
        val explicitSelection = e.getData(VcsDataKeys.CHANGES)
        
        if (explicitSelection != null && explicitSelection.isNotEmpty()) {
            return Pair(explicitSelection.toList(), emptyList())
        }

        return diffExtractor.getAllChanges()
    }

    private fun setCommitInputGeneratingState(e: AnActionEvent): CommitInputState {
        val control = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
        val controlObj = control as Any?
        val originalMessage = readCommitMessage(controlObj)

        writeCommitMessage(controlObj, "正在生成中…")
        setCommitInputEditable(controlObj, false)

        return CommitInputState(controlObj, originalMessage)
    }

    private fun restoreCommitInputState(
        e: AnActionEvent,
        inputState: CommitInputState?,
        generatedMessage: String?
    ) {
        val fallbackControl = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as Any?
        val state = inputState ?: CommitInputState(fallbackControl, "")
        val targetMessage = generatedMessage?.takeIf { it.isNotBlank() } ?: state.originalMessage

        writeCommitMessage(state.control, targetMessage)
        setCommitInputEditable(state.control, true)
    }

    private fun readCommitMessage(control: Any?): String {
        if (control == null) return ""

        if (control is JTextComponent) {
            return control.text.orEmpty()
        }

        val getter = control.javaClass.methods.firstOrNull {
            it.name == "getCommitMessage" && it.parameterCount == 0
        } ?: return ""

        return (runCatching { getter.invoke(control) as? String }.getOrNull()).orEmpty()
    }

    private fun writeCommitMessage(control: Any?, message: String) {
        if (control == null) return

        if (control is JTextComponent) {
            control.text = message
            return
        }

        val setter = control.javaClass.methods.firstOrNull {
            it.name == "setCommitMessage" && it.parameterCount == 1 && it.parameterTypes[0] == String::class.java
        } ?: return

        runCatching { setter.invoke(control, message) }
    }

    private fun setCommitInputEditable(control: Any?, editable: Boolean) {
        if (control == null) return

        if (control is JComponent) {
            control.isEnabled = editable
        }
        if (control is JTextComponent) {
            control.isEditable = editable
        }

        val editableSetter = control.javaClass.methods.firstOrNull {
            it.name == "setEditable" && it.parameterCount == 1 && it.parameterTypes[0] == Boolean::class.javaPrimitiveType
        }
        if (editableSetter != null) {
            runCatching { editableSetter.invoke(control, editable) }
        }

        val enabledSetter = control.javaClass.methods.firstOrNull {
            it.name == "setEnabled" && it.parameterCount == 1 && it.parameterTypes[0] == Boolean::class.javaPrimitiveType
        }
        if (enabledSetter != null) {
            runCatching { enabledSetter.invoke(control, editable) }
        }
    }

    private fun setGeneratingState(project: Project, generating: Boolean) {
        val key = project.locationHash
        if (generating) {
            generatingProjects[key] = true
        } else {
            generatingProjects.remove(key)
        }
    }

    private fun isGenerating(project: Project): Boolean {
        return generatingProjects[project.locationHash] == true
    }

    private fun buildUserErrorDetail(ex: Throwable): String {
        return buildString {
            appendLine("Error Type: ${ex.javaClass.name}")
            appendLine("Message: ${ex.message ?: "<empty>"}")
            appendLine("Details: Full stack trace was written to IDE log")
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val generating = isGenerating(project)
        e.presentation.isVisible = true
        e.presentation.isEnabled = !generating
        e.presentation.text = if (generating) ACTION_TEXT_LOADING else ACTION_TEXT_DEFAULT
    }
}
