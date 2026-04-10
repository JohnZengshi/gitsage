package com.example.gitsage.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitUtil
import git4idea.repo.GitRepository

object GitRepositoryUtils {

    fun getRepository(project: Project, file: VirtualFile?): GitRepository? {
        return GitUtil.getRepositoryManager(project).getRepositoryForFile(file)
    }

    fun getRepositories(project: Project): List<GitRepository> {
        return GitUtil.getRepositoryManager(project).repositories
    }

    fun getRepositoryRoot(project: Project, file: VirtualFile?): VirtualFile? {
        return getRepository(project, file)?.root
    }

    fun isGitRepository(project: Project): Boolean {
        return getRepositories(project).isNotEmpty()
    }
}
