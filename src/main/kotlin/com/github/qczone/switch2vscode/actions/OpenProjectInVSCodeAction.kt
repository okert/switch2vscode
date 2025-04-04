package com.github.qczone.switch2vscode.actions

import com.github.qczone.switch2vscode.settings.AppSettingsState
import com.github.qczone.switch2vscode.utils.WindowUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.actionSystem.ActionUpdateThread

class OpenProjectInVSCodeAction : AnAction() {
    private val logger = Logger.getInstance(OpenProjectInVSCodeAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val projectPath = project.basePath ?: return

        val settings = AppSettingsState.getInstance()
        val vscodePath = settings.vscodePath

        val command = when {
            System.getProperty("os.name").lowercase().contains("mac") -> {
                arrayOf("open", "-a", "$vscodePath", "--args", projectPath)
            }

            System.getProperty("os.name").lowercase().contains("windows") -> {
                arrayOf("cmd", "/c", "$vscodePath", projectPath)
            }

            else -> {
                arrayOf(vscodePath, projectPath)
            }
        }
        try {
            logger.info("Executing command: ${command.joinToString(" ")}")
            ProcessBuilder(*command).start()
        } catch (ex: Exception) {
            logger.error("Failed to execute VSCode command: ${ex.message}", ex)
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                """
                ${ex.message}
                
                Please check:
                1. VSCode path is correctly configured in Settings > Tools > Switch2VSCode
                2. VSCode is properly installed on your system
                3. The configured path points to a valid VSCode executable
                """.trimIndent(),
                "Error"
            )
        }

        WindowUtils.activeWindow()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
} 