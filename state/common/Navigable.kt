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

package com.vaticle.typedb.studio.state.common

interface Navigable<T : Navigable.Item<T>> {

    val entries: List<T>

    interface Item<T : Item<T>> : Comparable<Item<T>> {

        val name: String
        val container: Container<T>?
        val info: String?
        val isContainer: Boolean get() = false

        fun asContainer(): Container<T> {
            throw TypeCastException("Illegal cast of Navigable.Item to Navigable.Item.Expandable")
        }

        interface Container<T : Item<T>> : Item<T> {

            override val isContainer: Boolean get() = true
            override fun asContainer(): Container<T> {
                return this
            }

            val entries: List<T>
        }
    }

}
