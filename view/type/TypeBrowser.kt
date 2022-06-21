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

package com.vaticle.typedb.studio.view.type

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.state.GlobalState
import com.vaticle.typedb.studio.state.common.util.Label
import com.vaticle.typedb.studio.state.common.util.Sentence
import com.vaticle.typedb.studio.state.schema.TypeState
import com.vaticle.typedb.studio.view.concept.Concept.conceptIcon
import com.vaticle.typedb.studio.view.common.theme.Theme
import com.vaticle.typedb.studio.view.material.BrowserGroup
import com.vaticle.typedb.studio.view.material.ContextMenu
import com.vaticle.typedb.studio.view.material.Form
import com.vaticle.typedb.studio.view.material.Form.IconButtonArg
import com.vaticle.typedb.studio.view.material.Icon
import com.vaticle.typedb.studio.view.material.Navigator
import com.vaticle.typedb.studio.view.material.Navigator.rememberNavigatorState
import com.vaticle.typedb.studio.view.material.Tooltip

class TypeBrowser(isOpen: Boolean = false, order: Int) : BrowserGroup.Browser(isOpen, order) {

    override val label: String = Label.TYPES
    override val icon: Icon.Code = Icon.Code.SITEMAP
    override val isActive: Boolean get() = GlobalState.client.isConnected && GlobalState.client.session.isOpen
    override var buttons: List<IconButtonArg> by mutableStateOf(emptyList())

    @Composable
    override fun Content() {
        val client = GlobalState.client
        val schema = GlobalState.schema
        if (!client.isConnected) ConnectToServerHelper()
        else if (!client.isInteractiveMode) NonInteractiveModeMessage()
        else if (!client.session.isOpen || client.selectDBDialog.isOpen || !schema.isOpen) SelectDBHelper()
        else NavigatorLayout()
    }

    @Composable
    private fun NavigatorLayout() {
        val navState = rememberNavigatorState(
            container = GlobalState.schema,
            title = Label.TYPE_BROWSER,
            mode = Navigator.Mode.BROWSER,
            initExpandDepth = 1,
            contextMenuFn = { contextMenuItems(it) }
        ) { GlobalState.resource.tryOpen(it.item) }
        GlobalState.schema.onTypesChange = { navState.reloadEntries() }
        buttons = listOf(refreshButton(navState), exportButton(navState)) + navState.buttons
        Navigator.Layout(
            state = navState,
            modifier = Modifier.fillMaxSize(),
            iconArg = { conceptIcon(it.item.conceptType) }
        )
    }

    private fun refresh(navState: Navigator.NavigatorState<TypeState.Thing>) {
        GlobalState.schema.refreshReadTx()
        navState.reloadEntries()
    }

    private fun refreshButton(navState: Navigator.NavigatorState<TypeState.Thing>): IconButtonArg {
        return IconButtonArg(
            icon = Icon.Code.ROTATE,
            tooltip = Tooltip.Arg(title = Label.REFRESH)
        ) { refresh(navState) }
    }

    private fun exportButton(navState: Navigator.NavigatorState<TypeState.Thing>): IconButtonArg {
        return IconButtonArg(
            icon = Icon.Code.ARROW_UP_RIGHT_FROM_SQUARE,
            enabled = GlobalState.project.current != null,
            tooltip = Tooltip.Arg(title = Label.EXPORT_SCHEMA)
        ) {
            GlobalState.schema.exportTypeSchema { schema ->
                refresh(navState)
                GlobalState.project.tryCreateUntitledFile()?.let { file ->
                    file.content(schema)
                    GlobalState.resource.tryOpen(file)
                }
            }
        }
    }

    private fun contextMenuItems(itemState: Navigator.ItemState<TypeState.Thing>): List<List<ContextMenu.Item>> {
        val type = itemState.item
        return listOf(
            listOf(
                ContextMenu.Item(
                    label = Label.OPEN,
                    icon = Icon.Code.BLOCK_QUOTE
                ) { GlobalState.resource.tryOpen(type) },
            ),
            listOf(
                ContextMenu.Item(
                    label = Label.RENAME,
                    icon = Icon.Code.PEN,
                ) { type.initiateRename() },
                ContextMenu.Item(
                    label = Label.CHANGE_SUPERTYPE,
                    icon = Icon.Code.ARROW_UP_SMALL_BIG,
                    enabled = !type.isRoot,
                ) { type.initiateChangeSupertype() },
            ),
            listOf(
                ContextMenu.Item(
                    label = Label.DELETE,
                    icon = Icon.Code.TRASH_CAN,
                    enabled = !type.hasSubtypes,
                ) {
                    GlobalState.confirmation.submit(
                        title = Label.CONFIRM_TYPE_DELETION,
                        message = Sentence.CONFIRM_TYPE_DELETION,
                        onConfirm = { type.delete(); itemState.navState.reloadEntries() }
                    )
                }
            )
        )
    }

    @Composable
    private fun ConnectToServerHelper() {
        Box(Modifier.fillMaxSize().background(Theme.studio.backgroundLight), Alignment.Center) {
            Form.TextButton(
                text = Label.CONNECT_TO_TYPEDB,
                leadingIcon = Form.IconArg(Icon.Code.SERVER)
            ) { GlobalState.client.connectServerDialog.open() }
        }
    }

    @Composable
    private fun NonInteractiveModeMessage() {
        Box(Modifier.fillMaxSize().background(Theme.studio.backgroundLight), Alignment.Center) {
            Form.Text(
                value = Sentence.TYPE_BROWSER_ONLY_INTERACTIVE,
                modifier = Modifier.padding(30.dp),
                align = TextAlign.Center,
                softWrap = true
            )
        }
    }

    @Composable
    private fun SelectDBHelper() {
        Box(Modifier.fillMaxSize().background(Theme.studio.backgroundLight), Alignment.Center) {
            Form.TextButton(
                text = Label.SELECT_DATABASE,
                leadingIcon = Form.IconArg(Icon.Code.DATABASE)
            ) { GlobalState.client.selectDBDialog.open() }
        }
    }
}
