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

package com.vaticle.typedb.studio.view.navigator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vaticle.typedb.studio.state.State
import com.vaticle.typedb.studio.state.project.Directory
import com.vaticle.typedb.studio.state.project.File
import com.vaticle.typedb.studio.state.project.ProjectItem
import com.vaticle.typedb.studio.view.common.Label
import com.vaticle.typedb.studio.view.common.component.ContextMenu
import com.vaticle.typedb.studio.view.common.component.Form
import com.vaticle.typedb.studio.view.common.component.Icon
import com.vaticle.typedb.studio.view.common.component.Navigator
import com.vaticle.typedb.studio.view.common.component.Navigator.IconArgs
import com.vaticle.typedb.studio.view.common.theme.Theme
import mu.KotlinLogging

internal class ProjectBrowser(areaState: BrowserArea.AreaState, initOpen: Boolean = false) :
    Browser(areaState, initOpen) {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    override val label: String = Label.PROJECT
    override val icon: Icon.Code = Icon.Code.FOLDER_BLANK
    override val isActive: Boolean get() = State.project.current != null
    override var buttons: List<Form.ButtonArgs> by mutableStateOf(emptyList())

    @Composable
    override fun CatalogLayout() {
        if (!isActive) OpenProjectHelper()
        else {
            val navigator = Navigator(
                name = Label.PROJECT_NAVIGATOR,
                navigable = State.project.current!!,
                iconArgs = { projectItemIcon(it) },
                contextMenuFn = { contextMenuItems(it) }
            ) {
                when (it.value) {
                    is Directory -> it.toggle()
                    is File -> State.page.open(it.value.asFile())
                }
            }
            buttons = navigator.buttons
            navigator.entries[0].expand()
            navigator.Layout()
        }
    }

    @Composable
    private fun OpenProjectHelper() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(color = Theme.colors.disabled)
        ) {
            Form.TextButton(
                text = Label.OPEN_PROJECT,
                onClick = { State.project.showDialog = true },
                leadingIcon = Icon.Code.FOLDER_OPEN
            )
        }
    }

    private fun projectItemIcon(item: ProjectItem): IconArgs {
        return when (item) {
            is Directory -> when {
                item.isSymbolicLink -> IconArgs(Icon.Code.LINK_SIMPLE)
                item.isNavigated.value -> IconArgs(Icon.Code.FOLDER_OPEN)
                else -> IconArgs(Icon.Code.FOLDER_BLANK)
            }
            is File -> when {
                item.isTypeQL && item.isSymbolicLink -> IconArgs(Icon.Code.LINK_SIMPLE) { Theme.colors.secondary }
                item.isTypeQL -> IconArgs(Icon.Code.RECTANGLE_CODE) { Theme.colors.secondary }
                item.isSymbolicLink -> IconArgs(Icon.Code.LINK_SIMPLE)
                else -> IconArgs(Icon.Code.FILE_LINES)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun contextMenuItems(item: Navigator.ItemState<ProjectItem>): List<ContextMenu.Item> {
        return when (item.value) {
            is Directory -> directoryContextMenuItems(item)
            is File -> fileContextMenuItems(item.value.asFile())
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun directoryContextMenuItems(directory: Navigator.ItemState<ProjectItem>): List<ContextMenu.Item> {
        return listOf(
            ContextMenu.Item(Label.EXPAND_COLLAPSE, Icon.Code.FOLDER_OPEN) { directory.toggle() },
            ContextMenu.Item(Label.CREATE_DIRECTORY, Icon.Code.FOLDER_PLUS) { }, // TODO
            ContextMenu.Item(Label.CREATE_FILE, Icon.Code.FILE_PLUS) { }, // TODO
            ContextMenu.Item(Label.DELETE, Icon.Code.TRASH_CAN) { directory.value.delete() }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun fileContextMenuItems(file: File): List<ContextMenu.Item> {
        return listOf(
            ContextMenu.Item(Label.OPEN, Icon.Code.PEN) { State.page.open(file) },
            ContextMenu.Item(Label.DELETE, Icon.Code.TRASH_CAN) { file.delete() }
        )
    }
}
