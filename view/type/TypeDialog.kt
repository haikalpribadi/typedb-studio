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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.state.GlobalState
import com.vaticle.typedb.studio.state.common.util.Label
import com.vaticle.typedb.studio.view.material.Dialog
import com.vaticle.typedb.studio.view.material.Form

object TypeDialog {

    private val DIALOG_WIDTH = 500.dp
    private val DIALOG_HEIGHT = 200.dp

    @Composable
    fun MayShowDialogs() {
        val schemaMgr = GlobalState.schema
        if (schemaMgr.editLabelDialog.isOpen) EditLabelDialog()
        if (schemaMgr.editSupertypeDialog.isOpen) EditSupertypeDialog()
        if (schemaMgr.editAbstractDialog.isOpen) EditAbstractDialog()
    }

    @Composable
    private fun EditLabelDialog() {
        val editLabelDialog = GlobalState.schema.editLabelDialog
        val type = editLabelDialog.type!!
        val focusReq = remember { FocusRequester() }
        var newLabel by remember { mutableStateOf("") }
        val formState = remember {
            object : Form.State {
                override fun cancel() = editLabelDialog.close()
                override fun isValid(): Boolean = newLabel.isNotEmpty() && newLabel != type.name
                override fun trySubmit() = type.rename(newLabel)
            }
        }

        Dialog.Layout(editLabelDialog, Label.EDIT_TYPE_LABEL, DIALOG_WIDTH, DIALOG_HEIGHT) {
            Form.Submission(formState, Modifier.fillMaxSize(), submitLabel = Label.RENAME) {
                Form.Field(Label.LABEL) {
                    Form.TextInput(
                        value = type.name,
                        placeholder = "",
                        onValueChange = { newLabel = it },
                        modifier = Modifier.focusRequester(focusReq),
                    )
                }
            }
        }
    }

    @Composable
    private fun EditSupertypeDialog() {

    }

    @Composable
    private fun EditAbstractDialog() {

    }
}