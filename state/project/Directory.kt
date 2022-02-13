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

import com.vaticle.typedb.studio.state.common.Message.Project.Companion.DIRECTORY_NOT_DELETABLE
import com.vaticle.typedb.studio.state.common.Message.System.Companion.ILLEGAL_CAST
import com.vaticle.typedb.studio.state.common.Navigable
import com.vaticle.typedb.studio.state.common.Property
import com.vaticle.typedb.studio.state.notification.NotificationManager
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.isWritable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import mu.KotlinLogging

class Directory internal constructor(path: Path, parent: Directory?, notificationMgr: NotificationManager) :
    Navigable.ExpandableItem<ProjectItem>, ProjectItem(Type.DIRECTORY, path, parent, notificationMgr) {

    override var entries: List<ProjectItem> = emptyList()
    override val isReadable: Boolean get() = path.isReadable()
    override val isWritable: Boolean get() = path.isWritable()

    companion object {
        private const val UNTITLED = "Untitled"
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun asDirectory(): Directory {
        return this
    }

    override fun asFile(): File {
        throw TypeCastException(ILLEGAL_CAST.message(Directory::class.simpleName, File::class.simpleName))
    }

    override fun reloadEntries() {
        val new = path.listDirectoryEntries().filter { it.isReadable() }.toSet()
        val old = entries.map { it.path }.toSet()
        if (new != old) {
            val deleted = old - new
            val added = new - old
            entries = (entries.filter { !deleted.contains(it.path) } + added.map { projectItemOf(it) }).sorted()
        }
    }

    private fun projectItemOf(it: Path): ProjectItem {
        return if (it.isDirectory()) Directory(it, this, notificationMgr) else File(it, this, notificationMgr)
    }

    override fun delete() {
        try {
            reloadEntries()
            entries.filter { it.isDirectory }.forEach { it.delete() }
            entries.filter { it.isFile }.forEach { it.delete() }
            entries = emptyList()
            path.deleteExisting()
        } catch (e: Exception) {
            notificationMgr.userError(LOGGER, DIRECTORY_NOT_DELETABLE, path.name)
        }
    }

    fun nexUntitledDirName(): String {
        var counter = 1
        reloadEntries()
        while (entries.filter { it.name == UNTITLED + counter }.isNotEmpty()) counter++
        return UNTITLED + counter
    }

    fun nextUntitledFileName(): String {
        val format = Property.FileType.TYPEQL.extensions[0]
        var counter = 1
        reloadEntries()
        while (entries.filter { it.name == "$UNTITLED$counter.$format" }.isNotEmpty()) counter++
        return "$UNTITLED$counter.$format"
    }

    internal fun contains(newName: String): Boolean {
        reloadEntries()
        return entries.any { it.name == newName }
    }

    internal fun createDirectory(name: String): Directory {
        path.resolve(name).createDirectory()
        reloadEntries()
        return entries.first { it.name == name }.asDirectory()
    }

    internal fun createFile(name: String): File {
        path.resolve(name).createFile()
        reloadEntries()
        return entries.first { it.name == name }.asFile()
    }
}
