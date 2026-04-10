package com.example.gitsage.notifications

import com.example.gitsage.GitSagePlugin
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationHelper {
    private const val GROUP_ID = "GitSage"

    fun showInfo(project: Project?, message: String) {
        showNotification(project, message, NotificationType.INFORMATION)
    }

    fun showWarning(project: Project?, message: String) {
        showNotification(project, message, NotificationType.WARNING)
    }

    fun showError(project: Project?, message: String) {
        showNotification(project, message, NotificationType.ERROR)
    }

    private fun showNotification(project: Project?, message: String, type: NotificationType) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(message, type)

        if (project != null) {
            notification.notify(project)
        } else {
            notification.notify(null)
        }
    }
}
