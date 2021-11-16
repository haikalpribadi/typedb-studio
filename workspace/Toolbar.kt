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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.common.theme.StudioTheme
import com.vaticle.typedb.studio.common.component.Icon
import com.vaticle.typedb.studio.common.component.IconSize.*
import com.vaticle.typedb.studio.common.component.StudioDatabaseIcon
import com.vaticle.typedb.studio.common.component.StudioIcon

@Composable
fun Toolbar(
    modifier: Modifier = Modifier, dbName: String, allDBNames: List<String>, onDBNameChange: (value: String) -> Unit,
    onOpen: () -> Unit, onSave: () -> Unit, onRun: () -> Unit, onLogout: () -> Unit) {

    Row(modifier.height(28.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(8.dp))
        StudioDatabaseIcon()
        Spacer(Modifier.width(4.dp))
        Text(dbName, style = StudioTheme.typography.body2)
        Spacer(Modifier.width(8.dp))
        // TODO: implement db switcher
//        StudioDropdownBox(items = allDBNames, text = dbName, onTextChange = onDBNameChange, textStyle = StudioTheme.typography.body2,
//            modifier = Modifier.size(width = 96.dp, height = 24.dp),
//            leadingIcon = { StudioDatabaseIcon() })

//        Spacer(Modifier.width(8.dp))
//        StudioIcon(Icon.ChevronLeft, size = Size16Light)
//
//        Spacer(Modifier.width(8.dp))
//        StudioIcon(Icon.ChevronRight, size = Size16Light)

        Spacer(Modifier.width(10.dp))
        StudioIcon(Icon.FolderOpen, size = Size14, modifier = Modifier.clickable { onOpen() })

        Spacer(Modifier.width(12.dp))
        StudioIcon(Icon.FloppyDisk, modifier = Modifier.clickable { onSave() })

        Spacer(Modifier.width(10.dp))
        StudioIcon(Icon.Play, Color(0xFF499C54), size = Size18, modifier = Modifier.clickable { onRun() })

//        Spacer(Modifier.width(8.dp))
//        StudioIcon(Icon.Stop, Color(0xFFA1250C), size = Size18)

        Spacer(Modifier.weight(1F))

//        StudioIcon(Icon.Cog)
//        Spacer(Modifier.width(15.dp))

        StudioIcon(Icon.LogOut, modifier = Modifier.clickable { onLogout() })
        Spacer(Modifier.width(12.dp))
    }
}
