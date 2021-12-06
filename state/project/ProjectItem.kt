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

import com.vaticle.typedb.studio.state.common.Navigable
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.readSymbolicLink

sealed class ProjectItem(val path: Path, override val container: Directory?) : Navigable.Item<ProjectItem> {

    private val hash = Objects.hash(path, container)
    override val name = path.fileName.toString()
    override val info = if (path.isSymbolicLink()) "→ " + path.readSymbolicLink().toString() else null

    val absolutePath: Path = path.toAbsolutePath()
    val isSymbolicLink: Boolean = path.isSymbolicLink()
    open val isDirectory: Boolean = true
    open val isFile: Boolean = false

    abstract fun asDirectory(): Directory
    abstract fun asFile(): File

    fun delete() {
        // TODO
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ProjectItem
        return path != other.path && container != other.container
    }

    override fun hashCode(): Int {
        return hash
    }
}
