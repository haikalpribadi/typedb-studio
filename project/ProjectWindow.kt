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

package com.vaticle.typedb.studio.project

import com.vaticle.typedb.studio.common.Label
import com.vaticle.typedb.studio.service.Service
import javax.swing.JFileChooser


object ProjectWindow {

    fun Layout() {
        val directoryChooser = JFileChooser()
        directoryChooser.dialogTitle = Label.OPEN_PROJECT_DIRECTORY
        directoryChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val option = directoryChooser.showOpenDialog(null)
        if (option == JFileChooser.APPROVE_OPTION) {
            val file = directoryChooser.selectedFile
            println(file)
        } else {
            println("Open command canceled")
        }
        Service.project.showWindow = false
    }
}
