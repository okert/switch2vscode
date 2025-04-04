package com.github.qczone.switch2vscode.actions

import com.github.qczone.switch2vscode.settings.AppSettingsState
import com.github.qczone.switch2vscode.utils.WindowUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.actionSystem.ActionUpdateThread

class OpenFileInVSCodeAction : AnAction() {
    private val logger = Logger.getInstance(OpenFileInVSCodeAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val virtualFile: VirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        
        val line = editor?.caretModel?.logicalPosition?.line?.plus(1) ?: 1
        val column = editor?.caretModel?.logicalPosition?.column?.plus(1) ?: 1
        
        val filePath = virtualFile.path
        val settings = AppSettingsState.getInstance()
        val vscodePath = settings.vscodePath
        
        val command = when {
            System.getProperty("os.name").lowercase().contains("mac") -> {
                arrayOf("open", "-a", "$vscodePath", "--args", "--goto", "$filePath:$line:$column")
            }
            System.getProperty("os.name").lowercase().contains("windows") -> {
                arrayOf("cmd", "/c", "$vscodePath", "--goto", "$filePath:$line:$column")
            }
            else -> {
                arrayOf(vscodePath, "--goto", "$filePath:$line:$column")
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
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        
        e.presentation.isEnabledAndVisible = project != null && 
                                           virtualFile != null && 
                                           !virtualFile.isDirectory
    }
} 