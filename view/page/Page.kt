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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.state.page.Pageable
import com.vaticle.typedb.studio.state.project.File
import com.vaticle.typedb.studio.view.common.component.Form

abstract class Page(val state: Pageable) {

    companion object {
        @Composable
        fun of(state: Pageable): Page {
            return when (state) {
                is File -> FilePage.create(state)
                else -> throw IllegalStateException("should never be reached")
            }
        }
    }

    abstract val name: String
    abstract val isWritable: Boolean
    abstract val icon: Form.IconArgs

    var tabSize by mutableStateOf(0.dp)

    abstract fun updateState(state: Pageable)
    abstract fun resetFocus()

    @Composable
    abstract fun Layout()
}
