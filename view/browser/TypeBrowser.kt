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

package com.vaticle.typedb.studio.view.browser

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
import com.vaticle.typedb.studio.state.types.Type
import com.vaticle.typedb.studio.view.common.Label
import com.vaticle.typedb.studio.view.common.component.ContextMenu
import com.vaticle.typedb.studio.view.common.component.Form
import com.vaticle.typedb.studio.view.common.component.Form.ButtonArgs
import com.vaticle.typedb.studio.view.common.component.Icon
import com.vaticle.typedb.studio.view.common.component.Navigator
import com.vaticle.typedb.studio.view.common.theme.Theme
import com.vaticle.typedb.studio.view.dialog.SelectDatabaseDialog

internal class TypeBrowser(areaState: BrowserArea.AreaState, order: Int, initOpen: Boolean = false) :
    Browser(areaState, order, initOpen) {

    override val label: String = Label.TYPES
    override val icon: Icon.Code = Icon.Code.SITEMAP
    override val isActive: Boolean get() = State.connection.hasDatabase()
    override var buttons: List<ButtonArgs> by mutableStateOf(emptyList())

    @Composable
    override fun NavigatorLayout() {
        val selectDBDialogState = SelectDatabaseDialog.rememberState()
        if (!State.connection.isConnected()) ConnectToServerHelper()
        else if (!State.connection.hasDatabase() || selectDBDialogState.showDialog) SelectDBHelper(selectDBDialogState)
        else {
            val state = Navigator.rememberNavigatorState(
                container = State.types.root!!,
                title = Label.TYPE_BROWSER,
                initExpandDepth = 1,
                liveUpdate = true
            ) { typeOpen(it) }
            buttons = state.buttons
            Navigator.Layout(navState = state, iconArgs = { typeIcon(it) }) { contextMenuItems(it) }
        }
    }

    private fun typeOpen(itemState: Navigator.ItemState<Type>) {

    }

    private fun typeIcon(itemState: Navigator.ItemState<Type>): Form.IconArgs {

    }

    private fun contextMenuItems(itemState: Navigator.ItemState<Type>): List<ContextMenu.Item> {

    }

    @Composable
    private fun ConnectToServerHelper() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(color = Theme.colors.disabled)
        ) {
            Form.TextButton(
                text = Label.CONNECT_TO_TYPEDB,
                onClick = { State.connection.showDialog = true },
                leadingIcon = Icon.Code.DATABASE
            )
        }
    }

    @Composable
    private fun SelectDBHelper(selectDBDialogState: SelectDatabaseDialog.DialogState) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(color = Theme.colors.disabled)
        ) {
            Form.TextButton(
                text = Label.SELECT_DATABASE,
                onClick = { selectDBDialogState.showDialog = true },
                leadingIcon = Icon.Code.DATABASE
            )
        }
        if (selectDBDialogState.showDialog) SelectDatabaseDialog.Layout(selectDBDialogState)
    }
}
