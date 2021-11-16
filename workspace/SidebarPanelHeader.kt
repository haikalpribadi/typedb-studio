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

package com.vaticle.typedb.studio.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.appearance.StudioTheme
import com.vaticle.typedb.studio.common.composable.Icon
import com.vaticle.typedb.studio.common.composable.StudioIcon

@Composable
fun SidebarPanelHeader(title: String, onCollapse: () -> Unit) {
    PanelHeader(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.width(6.dp))
        Text(title, style = StudioTheme.typography.body2)

        Spacer(Modifier.weight(1f))

        StudioIcon(Icon.Minus, modifier = Modifier.width(16.dp).clickable { onCollapse() })
        Spacer(Modifier.width(12.dp))
    }
}
