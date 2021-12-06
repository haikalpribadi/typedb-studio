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

package com.vaticle.typedb.studio.state.project

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.vaticle.typedb.studio.state.common.Navigable
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

class Directory internal constructor(path: Path, parent: Directory?) :
    Navigable.Item.Container<ProjectItem>, ProjectItem(path, parent) {

    override var isNavigated: MutableState<Boolean> = mutableStateOf(false); private set
    override var entries: MutableState<List<ProjectItem>> = mutableStateOf(emptyList())
    override val isDirectory: Boolean = true

    override fun asDirectory(): Directory {
        return this
    }

    override fun asFile(): File {
        throw TypeCastException("Invalid casting of Directory to File") // TODO: generalise
    }

    override fun reloadEntries() {
        val newPaths = path.listDirectoryEntries()
        val updatedDirs = updatedDirs(newPaths.filter { it.isDirectory() }.toSet())
        val updatedFiles = updatedFiles(newPaths.filter { it.isRegularFile() }.toSet())
        entries.value = updatedDirs.sortedBy { it.name } + updatedFiles.sortedBy { it.name }
        entries.value.filterIsInstance<Directory>().filter { it.isNavigated.value }.forEach { it.reloadEntries() }
    }

    private fun updatedDirs(new: Set<Path>): List<Directory> {
        val old = entries.value.filter { it.isDirectory }.map { it.path }.toSet()
        val deleted = old - new
        val added = new - old
        return entries.value.filterIsInstance<Directory>().filter { !(deleted).contains(it.path) } +
                (added).map { Directory(it, this) }
    }

    private fun updatedFiles(new: Set<Path>): List<File> {
        val old = entries.value.filter { it.isFile }.map { it.path }.toSet()
        val deleted = old - new
        val added = new - old
        return entries.value.filterIsInstance<File>().filter { !(deleted).contains(it.path) } +
                (added).map { File(it, this) }
    }

    internal fun checkForUpdate() {
        if (!isNavigated.value) return
        val new = path.listDirectoryEntries().toSet()
        val old = entries.value.map { it.path }.toSet()
        if (new != old) reloadEntries()
        entries.value.filterIsInstance<Directory>().filter { it.isNavigated.value }.forEach { it.checkForUpdate() }
    }
}
