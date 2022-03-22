/*
 * Copyright (C) 2021 Vaticle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.vaticle.typedb.studio.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.vaticle.typedb.common.collection.Either
import com.vaticle.typedb.studio.state.GlobalState
import com.vaticle.typedb.studio.state.common.Message
import com.vaticle.typedb.studio.state.config.UserDataDirectory
import com.vaticle.typedb.studio.view.browser.BrowserArea
import com.vaticle.typedb.studio.view.common.Context.LocalWindow
import com.vaticle.typedb.studio.view.common.KeyMapper
import com.vaticle.typedb.studio.view.common.Label
import com.vaticle.typedb.studio.view.common.Sentence
import com.vaticle.typedb.studio.view.common.component.Form.Text
import com.vaticle.typedb.studio.view.common.component.Form.TextSelectable
import com.vaticle.typedb.studio.view.common.component.Frame
import com.vaticle.typedb.studio.view.common.component.Separator
import com.vaticle.typedb.studio.view.common.theme.Theme
import com.vaticle.typedb.studio.view.dialog.ConfirmationDialog
import com.vaticle.typedb.studio.view.dialog.ConnectionDialog
import com.vaticle.typedb.studio.view.dialog.DatabaseDialog
import com.vaticle.typedb.studio.view.dialog.ProjectDialog
import com.vaticle.typedb.studio.view.page.PageArea
import javax.swing.UIManager
import kotlin.system.exitProcess
import mu.KotlinLogging

object Studio {

    private val ERROR_WINDOW_WIDTH: Dp = 1000.dp
    private val ERROR_WINDOW_HEIGHT: Dp = 610.dp
    private val LOGGER = KotlinLogging.logger {}

    private fun getMainWindowTitle(): String {
        val pageName = GlobalState.resource.active?.fullName ?: GlobalState.project.current?.directory?.name ?: ""
        return Label.TYPEDB_STUDIO + " — " + pageName
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            setConfigurations()
            Message.loadClasses()
            UserDataDirectory.initialise()
            application { MainWindow(it) }
        } catch (exception: Exception) {
            LOGGER.error(exception.message, exception)
            application { ErrorWindow(exception, it) }
        } finally {
            LOGGER.debug { Label.CLOSING_TYPEDB_STUDIO }
            exitProcess(0)
        }
    }

    private fun setConfigurations() {
        // Enable anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on")
        System.setProperty("swing.aatext", "true")
        // Enable FileDialog to select "directories" on MacOS
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        // Enable native Windows UI style
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) // Set UI style for Windows
    }

    private fun application(window: @Composable (onExit: () -> Unit) -> Unit) {
        androidx.compose.ui.window.application {
            Theme.Material {
                window {
                    GlobalState.confirmation.submit(
                        title = Label.CONFIRM_QUITTING_APPLICATION,
                        message = Sentence.CONFIRM_QUITING_APPLICATION,
                        onConfirm = { exitApplication() } // TODO: we don't want to call exitApplication() for MacOS
                    )
                }
            }
        }
    }

    private fun handleKeyEvent(event: KeyEvent, onClose: () -> Unit): Boolean {
        return if (event.type == KeyEventType.KeyUp) false
        else KeyMapper.CURRENT.map(event)?.let { executeCommand(it, onClose) } ?: false
    }

    private fun executeCommand(command: KeyMapper.Command, onClose: () -> Unit): Boolean {
        return when (command) {
            KeyMapper.Command.QUIT -> {
                onClose()
                true
            }
            else -> false
        }
    }

    @Composable
    private fun MainWindow(onClose: () -> Unit) {
        // TODO: we want no title bar, by passing undecorated=true, but it seems to cause intermittent crashes on startup
        //       (see #40). Test if they occur when running the distribution, or only with bazel run :studio-bin-*
        Window(
            title = getMainWindowTitle(),
            onCloseRequest = { onClose() },
            state = rememberWindowState(WindowPlacement.Maximized),
            onPreviewKeyEvent = { handleKeyEvent(it, onClose) },
        ) {
            CompositionLocalProvider(LocalWindow provides window) {
                Column(Modifier.fillMaxSize().background(Theme.colors.background1)) {
                    Toolbar.Layout()
                    Separator.Horizontal()
                    Frame.Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        separator = Frame.SeparatorArgs(Separator.WEIGHT),
                        Frame.Pane(
                            id = BrowserArea.javaClass.name,
                            minSize = BrowserArea.MIN_WIDTH,
                            initSize = Either.first(BrowserArea.WIDTH)
                        ) { BrowserArea.Layout(it) },
                        Frame.Pane(
                            id = PageArea.javaClass.name,
                            minSize = PageArea.MIN_WIDTH,
                            initSize = Either.second(1f)
                        ) { PageArea.Layout() }
                    )
                    Separator.Horizontal()
                    StatusBar.Layout()
                }
                if (GlobalState.notification.queue.isNotEmpty()) NotificationArea.Layout()
                if (GlobalState.confirmation.isOpen) ConfirmationDialog.Layout()
                if (GlobalState.connection.connectServerDialog.isOpen) ConnectionDialog.ConnectServer()
                if (GlobalState.connection.manageDatabasesDialog.isOpen) DatabaseDialog.ManageDatabases()
                if (GlobalState.connection.selectDatabaseDialog.isOpen) DatabaseDialog.SelectDatabase()
                if (GlobalState.project.createItemDialog.isOpen) ProjectDialog.CreateProjectItem()
                if (GlobalState.project.openProjectDialog.isOpen) ProjectDialog.OpenProject()
                if (GlobalState.project.moveDirectoryDialog.isOpen) ProjectDialog.MoveDirectory()
                if (GlobalState.project.renameDirectoryDialog.isOpen) ProjectDialog.RenameDirectory()
                if (GlobalState.project.saveFileDialog.isOpen) ProjectDialog.SaveFile(window)
                if (GlobalState.project.renameFileDialog.isOpen) ProjectDialog.RenameFile()
            }
        }
    }

    @Composable
    private fun ErrorWindow(exception: Exception, onClose: () -> Unit) {
        Window(
            title = Label.TYPEDB_STUDIO_APPLICATION_ERROR,
            onCloseRequest = { onClose() },
            state = rememberWindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition.Aligned(Alignment.Center),
                size = DpSize(ERROR_WINDOW_WIDTH, ERROR_WINDOW_HEIGHT),
            )
        ) {
            Column(modifier = Modifier.fillMaxSize().background(Theme.colors.background1).padding(5.dp)) {
                val rowVerticalAlignment = Alignment.Top
                val rowModifier = Modifier.padding(5.dp)
                val labelModifier = Modifier.width(40.dp)
                val labelStyle = Theme.typography.body1.copy(fontWeight = FontWeight.Bold)
                val contentColor = Theme.colors.error2
                Row(verticalAlignment = rowVerticalAlignment, modifier = rowModifier) {
                    Text(value = "${Label.TITLE}:", modifier = labelModifier, textStyle = labelStyle)
                    exception.message?.let { TextSelectable(value = it, color = contentColor) }
                }
                Row(verticalAlignment = rowVerticalAlignment, modifier = rowModifier) {
                    Text(value = "${Label.TRACE}:", modifier = labelModifier, textStyle = labelStyle)
                    TextSelectable(value = exception.stackTraceToString(), color = contentColor)
                }
            }
        }
    }
}
