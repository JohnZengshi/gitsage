package com.example.gitsage.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object CredentialsManager {
    private const val SERVICE_NAME = "GitSagePlugin"

    fun saveApiKey(providerId: String, apiKey: String) {
        val attributes = createCredentialAttributes(providerId)
        val credentials = Credentials(providerId, apiKey)
        PasswordSafe.instance.set(attributes, credentials)
    }

    fun getApiKey(providerId: String): String? {
        val attributes = createCredentialAttributes(providerId)
        return PasswordSafe.instance.getPassword(attributes)
    }

    fun removeApiKey(providerId: String) {
        val attributes = createCredentialAttributes(providerId)
        PasswordSafe.instance.set(attributes, null)
    }

    private fun createCredentialAttributes(providerId: String): CredentialAttributes {
        return CredentialAttributes(
            generateServiceName(SERVICE_NAME, providerId)
        )
    }
}
