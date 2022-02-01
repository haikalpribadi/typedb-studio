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

package com.vaticle.typedb.studio.view.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.view.common.Label
import com.vaticle.typedb.studio.view.common.component.Form
import com.vaticle.typedb.studio.view.common.component.Icon
import com.vaticle.typedb.studio.view.common.component.Separator
import com.vaticle.typedb.studio.view.common.theme.Theme

object TextToolbar {

    private val MAX_WIDTH = 500.dp
    private val MIN_WIDTH = 260.dp
    private val ROW_HEIGHT = 28.dp
    private val BUTTON_AREA_WIDTH = 160.dp
    private val BUTTON_HEIGHT = 24.dp
    private val BUTTON_SPACING = 4.dp

    @Composable
    internal fun Area(state: TextFinder) {
        Row {
            FindAndReplaceTextInputs(state)
            Separator.Vertical()
            FinderToggles(state)
            Buttons(state)
            Separator.Horizontal()
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun FindAndReplaceTextInputs(state: TextFinder) {
        Column(Modifier.width(MAX_WIDTH)) {
            Form.TextInput(
                value = state.findText,
                placeholder = Label.FIND,
                onValueChange = { state.findText = it },
                leadingIcon = Icon.Code.MAGNIFYING_GLASS,
                shape = null,
                border = null,
                modifier = Modifier.weight(1f),
                // TODO: figure out how to set min width to MIN_WIDTH
            )
            if (state.showReplacer) {
                Separator.Horizontal()
                Form.TextInput(
                    value = state.replaceText,
                    placeholder = Label.REPLACE,
                    onValueChange = { state.replaceText = it },
                    leadingIcon = Icon.Code.RIGHT_LEFT,
                    shape = null,
                    border = null,
                    modifier = Modifier.weight(1f),
                    // TODO: figure out how to set min width to MIN_WIDTH
                )
            }
        }
    }

    @Composable
    private fun FinderToggles(state: TextFinder) {
        Form.IconButton(
            Icon.Code.FONT_CASE,
            onClick = { state.toggleCaseSensitive() },
            modifier = Modifier.size(ROW_HEIGHT),
            iconColor = if (state.isCaseSensitive) Theme.colors.secondary else Theme.colors.icon,
            bgColor = Theme.colors.surface,
            rounded = false
        )
    }

    private fun Buttons(state: TextFinder) {
        Column {
            FinderButtons(state)
            ReplacerButton(state)
        }
    }

    @Composable
    private fun FinderButtons(state: TextFinder) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(ROW_HEIGHT).width(BUTTON_AREA_WIDTH)
        ) {
            Spacer(Modifier.width(BUTTON_SPACING))
            Form.IconButton(
                icon = Icon.Code.CHEVRON_DOWN,
                onClick = { state },
                modifier = Modifier.size(ROW_HEIGHT),
                bgColor = Color.Transparent,
                rounded = false
            )
            Form.IconButton(
                icon = Icon.Code.CHEVRON_UP,
                onClick = { state },
                modifier = Modifier.size(ROW_HEIGHT),
                bgColor = Color.Transparent,
                rounded = false
            )
            Spacer(Modifier.width(BUTTON_SPACING))
            Form.Text(
                value = state.status,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun ReplacerButton(state: TextFinder) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.height(ROW_HEIGHT).width(BUTTON_AREA_WIDTH)
        ) {
            Spacer(Modifier.width(BUTTON_SPACING))
            Form.TextButton(Label.REPLACE, { state }, Modifier.height(BUTTON_HEIGHT))
            Spacer(Modifier.width(BUTTON_SPACING))
            Form.TextButton(Label.REPLACE_ALL, { state }, Modifier.height(BUTTON_HEIGHT))
        }
    }
}