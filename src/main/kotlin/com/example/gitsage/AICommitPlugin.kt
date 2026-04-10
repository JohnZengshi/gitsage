package com.example.gitsage

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class GitSagePlugin : ProjectActivity {
    private val logger = Logger.getInstance(GitSagePlugin::class.java)

    override suspend fun execute(project: Project) {
        logger.info("GitSage plugin initialized")
    }

    companion object {
        const val PLUGIN_ID = "com.example.gitsage"
    }
}
