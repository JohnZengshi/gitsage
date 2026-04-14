package com.example.gitsage.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationHelper {
    private const val GROUP_ID = "GitSage"
    private const val MAX_DETAIL_LINES = 12
    private const val MAX_DETAIL_CHARS = 1200

    fun showInfo(project: Project?, message: String) {
        showNotification(project, message, NotificationType.INFORMATION)
    }

    fun showWarning(project: Project?, message: String) {
        showNotification(project, message, NotificationType.WARNING)
    }

    fun showError(project: Project?, message: String) {
        showNotification(project, message, NotificationType.ERROR)
    }

    fun showDetailedError(project: Project?, title: String, detail: String) {
        val compactDetail = compactDetail(detail)
        val escapedDetail = escapeHtml(compactDetail)
        val content = "<pre style='margin-top:6px;'>$escapedDetail</pre>"

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(content, NotificationType.ERROR)

        notification.isImportant = true
        notification.setTitle(title)
        notify(project, notification)
    }

    private fun showNotification(project: Project?, message: String, type: NotificationType) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(message, type)

        notify(project, notification)
    }

    private fun notify(project: Project?, notification: Notification) {
        if (project != null) {
            notification.notify(project)
        } else {
            notification.notify(null)
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private fun compactDetail(detail: String): String {
        val normalized = detail.trim()
        if (normalized.isEmpty()) return "Unknown error"

        val lines = normalized.lineSequence().toList()
        val lineOverflow = lines.size > MAX_DETAIL_LINES
        val limitedByLines = if (lineOverflow) lines.take(MAX_DETAIL_LINES) else lines

        val joined = limitedByLines.joinToString("\n")
        val charOverflow = joined.length > MAX_DETAIL_CHARS
        val truncated = if (charOverflow) joined.take(MAX_DETAIL_CHARS) else joined

        return if (lineOverflow || charOverflow) {
            "$truncated\n... (truncated, see IDE logs for full details)"
        } else {
            truncated
        }
    }
}
