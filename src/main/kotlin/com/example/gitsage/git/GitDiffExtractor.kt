package com.example.gitsage.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.SimpleContentRevision
import git4idea.GitUtil
import git4idea.repo.GitRepository
import com.intellij.openapi.vfs.VirtualFile
import git4idea.index.GitFileStatus

class GitDiffExtractor(private val project: Project) {

    fun getSelectedChanges(): List<Change> {
        val changeListManager = ChangeListManager.getInstance(project)
        val defaultChangeList = changeListManager.defaultChangeList
        return defaultChangeList.changes.toList()
    }

    fun getAllChanges(): Pair<List<Change>, List<FilePath>> {
        val changeListManager = ChangeListManager.getInstance(project)
        val defaultChangeList = changeListManager.defaultChangeList
        val changes = defaultChangeList.changes.toMutableList()
        
        val unversionedFiles = changeListManager.unversionedFilesPaths
        
        return Pair(changes, unversionedFiles)
    }

    fun getStagedChanges(): List<Change> {
        val changeListManager = ChangeListManager.getInstance(project)
        val defaultChangeList = changeListManager.defaultChangeList
        return defaultChangeList.changes.filter { it.fileStatus != com.intellij.openapi.vcs.FileStatus.UNKNOWN }
    }

    fun buildDiff(changes: List<Change>, unversionedFiles: List<FilePath> = emptyList()): String {
        if (changes.isEmpty() && unversionedFiles.isEmpty()) {
            return ""
        }

        val diffBuilder = StringBuilder()

        for (change in changes) {
            val beforePath = change.beforeRevision?.file?.path ?: ""
            val afterPath = change.afterRevision?.file?.path ?: ""

            when {
                change.type == Change.Type.NEW -> {
                    diffBuilder.appendLine("diff --git a/$afterPath b/$afterPath")
                    diffBuilder.appendLine("new file mode 100644")
                    diffBuilder.appendLine("--- /dev/null")
                    diffBuilder.appendLine("+++ b/$afterPath")
                    diffBuilder.appendLine("@@ -0,0 +1,${getLineCount(change.afterRevision?.content) ?: 0} @@")
                    change.afterRevision?.content?.let { content ->
                        content.lines().forEach { line ->
                            diffBuilder.appendLine("+$line")
                        }
                    }
                }
                change.type == Change.Type.DELETED -> {
                    diffBuilder.appendLine("diff --git a/$beforePath b/$beforePath")
                    diffBuilder.appendLine("deleted file mode 100644")
                    diffBuilder.appendLine("--- a/$beforePath")
                    diffBuilder.appendLine("+++ /dev/null")
                    diffBuilder.appendLine("@@ -1,${getLineCount(change.beforeRevision?.content) ?: 0} +0,0 @@")
                    change.beforeRevision?.content?.let { content ->
                        content.lines().forEach { line ->
                            diffBuilder.appendLine("-$line")
                        }
                    }
                }
                else -> {
                    diffBuilder.appendLine("diff --git a/$beforePath b/$afterPath")
                    diffBuilder.appendLine("--- a/$beforePath")
                    diffBuilder.appendLine("+++ b/$afterPath")
                    val beforeContent = change.beforeRevision?.content ?: ""
                    val afterContent = change.afterRevision?.content ?: ""
                    diffBuilder.append(generateUnifiedDiff(beforeContent, afterContent))
                }
            }
            diffBuilder.appendLine()
        }

        for (filePath in unversionedFiles) {
            val path = filePath.path
            val content = readFileContent(filePath)
            
            diffBuilder.appendLine("diff --git a/$path b/$path")
            diffBuilder.appendLine("new file mode 100644")
            diffBuilder.appendLine("--- /dev/null")
            diffBuilder.appendLine("+++ b/$path")
            diffBuilder.appendLine("@@ -0,0 +1,${getLineCount(content) ?: 0} @@")
            content?.lines()?.forEach { line ->
                diffBuilder.appendLine("+$line")
            }
            diffBuilder.appendLine()
        }

        return diffBuilder.toString()
    }

    private fun readFileContent(filePath: FilePath): String? {
        return try {
            val virtualFile = filePath.virtualFile
            if (virtualFile != null && !virtualFile.isDirectory) {
                String(virtualFile.contentsToByteArray(), virtualFile.charset)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getLineCount(content: String?): Int? {
        return content?.lines()?.size
    }

    private fun generateUnifiedDiff(before: String, after: String): String {
        val beforeLines = before.lines()
        val afterLines = after.lines()

        return generateSimpleDiff(beforeLines, afterLines)
    }

    private fun generateSimpleDiff(before: List<String>, after: List<String>): String {
        val diff = StringBuilder()
        val maxLines = maxOf(before.size, after.size)

        for (i in 0 until maxLines) {
            val beforeLine = before.getOrNull(i)
            val afterLine = after.getOrNull(i)

            when {
                beforeLine == null && afterLine != null -> {
                    diff.appendLine("+$afterLine")
                }
                beforeLine != null && afterLine == null -> {
                    diff.appendLine("-$beforeLine")
                }
                beforeLine != afterLine -> {
                    diff.appendLine("-$beforeLine")
                    diff.appendLine("+$afterLine")
                }
            }
        }

        return diff.toString()
    }
}
