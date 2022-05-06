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

package com.vaticle.typedb.studio.view.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.common.collection.Either
import com.vaticle.typedb.studio.view.common.theme.Theme

object Table {

    val ROW_HEIGHT = 34.dp

    @Composable
    private fun bg(i: Int): Color = if (i % 2 == 0) Theme.colors.background2 else Theme.colors.background1

    @Composable
    fun <T> Layout(
        items: List<T>,
        modifier: Modifier = Modifier,
        headers: List<AnnotatedString?>,
        rowHeight: Dp = ROW_HEIGHT,
        colWeights: List<Either<Dp, Float>>,
        cellFns: List<@Composable (T) -> Unit>
    ) {
        assert(headers.size == colWeights.size && headers.size == cellFns.size) { "Size of each list needs to be equal" }
        LazyColumn(modifier.border(1.dp, Theme.colors.border)) {
            item {
                Row(Modifier.fillMaxWidth().height(rowHeight)) {
                    headers.forEachIndexed { i, header ->
                        Box(colWeights[i].apply({ Modifier.width(it) }, { Modifier.weight(it) })) {
                            header?.let { Form.Text(it) }
                        }
                    }
                }
            }
            items(items.count()) {
                Row(Modifier.fillMaxWidth().height(rowHeight)) {
                    cellFns.forEachIndexed { i, fn ->
                        Box(colWeights[i].apply({ Modifier.width(it) }, { Modifier.weight(it) }).background(bg(i))) {
                            fn(items[it])
                        }
                    }
                }
            }
        }
    }
}