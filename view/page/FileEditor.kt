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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaticle.typedb.studio.state.project.File
import com.vaticle.typedb.studio.view.common.component.Separator
import com.vaticle.typedb.studio.view.common.theme.Theme
import kotlin.math.ceil

object FileEditor {

    private val LINE_HEIGHT = 1.2
    private val LINE_NUMBER_SPACE = 4.dp

    @Composable
    fun Area(file: File, modifier: Modifier = Modifier) {
        val font = Theme.typography.code1
        val lineHeight = ceil(font.fontSize.value * LocalDensity.current.density * LINE_HEIGHT).sp
        var value: TextFieldValue by remember { mutableStateOf(highlight(file.content)) }

        BasicTextField(
            value = value,
            onValueChange = { file.content = it.text; value = it },
            cursorBrush = SolidColor(Theme.colors.secondary),
            textStyle = font.copy(color = Theme.colors.onBackground, lineHeight = lineHeight),
            modifier = modifier.background(Theme.colors.background2),
            decorationBox = { innerTextField ->
                Row {
                    Separator.Vertical(LINE_NUMBER_SPACE, Theme.colors.background)
                    Column(
                        modifier = Modifier.background(Theme.colors.background),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1 until file.content.split("\n").size) {
                            Text(
                                text = i.toString(),
                                style = font.copy(
                                    color = Theme.colors.onBackground.copy(alpha = 0.5f),
                                    lineHeight = lineHeight
                                ),
                            )
                        }
                    }
                    Separator.Vertical(LINE_NUMBER_SPACE, Theme.colors.background)
                    Separator.Vertical()
                    Separator.Vertical(LINE_NUMBER_SPACE, Theme.colors.background2)
                    innerTextField()
                }
            }
        )
    }

    private fun highlight(content: String): TextFieldValue {
        return TextFieldValue(AnnotatedString(content)) // TODO
    }
}