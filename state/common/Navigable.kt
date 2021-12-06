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

import androidx.compose.runtime.MutableState

interface Navigable<T : Navigable.Item<T>> {

    val entries: MutableState<List<T>>

    interface Item<U : Item<U>> {

        val name: String
        val container: U?
        val info: String?
        val isContainer: Boolean get() = false

        fun asContainer(): Container<U> {
            throw TypeCastException("Illegal cast of Catalog.Item to Catalog.Item.Expandable")
        }

        interface Container<V : Item<V>> : Item<V> {

            override val isContainer: Boolean get() = true
            override fun asContainer(): Container<V> {
                return this
            }

            val isNavigated: MutableState<Boolean>
            val entries: MutableState<List<V>>
            fun reloadEntries()
        }
    }
}
