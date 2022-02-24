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

package com.vaticle.typedb.studio.view.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vaticle.typedb.studio.state.page.Pageable
import com.vaticle.typedb.studio.state.project.File
import com.vaticle.typedb.studio.view.common.component.Form
import com.vaticle.typedb.studio.view.common.component.Icon
import com.vaticle.typedb.studio.view.common.theme.Theme
import com.vaticle.typedb.studio.view.editor.TextEditor

class FilePage private constructor(val file: File, private val editorState: TextEditor.State) : Page(file) {

    override val name: String get() = file.name
    override val isWritable: Boolean get() = file.isWritable
    override val icon: Form.IconArgs = when {
        file.isTypeQL -> Form.IconArgs(Icon.Code.RECTANGLE_CODE) { Theme.colors.secondary }
        else -> Form.IconArgs(Icon.Code.FILE_LINES)
    }

    companion object {
        @Composable
        fun create(file: File): FilePage {
            val editorState = TextEditor.createState(file)
            file.onWatch { if (editorState.isFocusable) editorState.focusReq.requestFocus() }
            return FilePage(file, editorState)
        }

    }

    override fun updateState(state: Pageable) {
        editorState.updateFile(state as File)
    }

    override fun resetFocus() {
        editorState.isFocusable = false
    }

    @Composable
    override fun Layout() {
        TextEditor.Area(state = editorState, modifier = Modifier.fillMaxSize())
    }
}
