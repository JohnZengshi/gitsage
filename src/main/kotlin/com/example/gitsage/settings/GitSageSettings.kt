package com.example.gitsage.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "GitSageSettings",
    storages = [Storage("ai-commit-settings.xml")]
)
class GitSageSettings : PersistentStateComponent<GitSageSettingsState> {
    private var state = GitSageSettingsState()

    override fun getState(): GitSageSettingsState = state

    override fun loadState(state: GitSageSettingsState) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    fun getSelectedProvider(): AIProviderConfig? {
        return state.providers.find { it.id == state.selectedProviderId }
    }

    fun saveProvider(config: AIProviderConfig) {
        val index = state.providers.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            state.providers[index] = config
        } else {
            state.providers.add(config)
        }
        if (config.apiKey.isNotEmpty()) {
            CredentialsManager.saveApiKey(config.id, config.apiKey)
            config.apiKey = ""
        }
    }

    fun removeProvider(providerId: String) {
        state.providers.removeIf { it.id == providerId }
        CredentialsManager.removeApiKey(providerId)
        if (state.selectedProviderId == providerId && state.providers.isNotEmpty()) {
            state.selectedProviderId = state.providers.first().id
        }
    }

    fun setSelectedProvider(providerId: String) {
        state.selectedProviderId = providerId
    }

    fun setConvention(convention: CommitConvention) {
        state.convention = convention
    }

    fun setLanguage(language: GenerationLanguage) {
        state.language = language
    }

    companion object {
        @JvmStatic
        fun getInstance(): GitSageSettings {
            return ApplicationManager.getApplication().getService(GitSageSettings::class.java)
        }
    }
}
