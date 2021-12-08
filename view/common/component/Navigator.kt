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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.state.common.Navigable

object Navigator {

    private val ITEM_HEIGHT = 26.dp
    private val ICON_WIDTH = 20.dp
    private val TEXT_SPACING = 4.dp
    private val AREA_PADDING = 8.dp

    private fun <T : Navigable.Item<T>> itemStateOf(
        item: Navigable.Item<T>, container: ItemState.Expandable<T>?
    ): ItemState<T> {
        return if (item.isContainer) ItemState.Expandable(item.asContainer(), container) else ItemState(item, container)
    }

    open class ItemState<T : Navigable.Item<T>> internal constructor(
        open val item: Navigable.Item<T>, val container: Expandable<T>?
    ) : Comparable<ItemState<T>> {

        open val isExpandable: Boolean = false
        val name get() = item.name
        val info get() = item.info

        open fun asExpandable(): Expandable<T> {
            throw TypeCastException("Illegal cast of Navigator.ItemState to Navigator.ItemState.Expandable")
        }

        override fun compareTo(other: ItemState<T>): Int {
            return item.compareTo(other.item)
        }

        class Expandable<T : Navigable.Item<T>> internal constructor(
            override val item: Navigable.Item.Container<T>, container: Expandable<T>?
        ) : ItemState<T>(item, container) {

            override val isExpandable: Boolean = true
            var isExpanded: Boolean by mutableStateOf(false)
            var entries: List<ItemState<T>> by mutableStateOf(emptyList())

            override fun asExpandable(): Expandable<T> {
                return this
            }

            fun expand() {
                toggle(true)
            }

            fun collapse() {
                toggle(false)
            }

            private fun toggle(isExpanded: Boolean) {
                this.isExpanded = isExpanded
                if (isExpanded) reloadEntries()
            }

            private fun reloadEntries() {
                val new = item.entries.toSet()
                val old = entries.map { it.item }.toSet()
                val deleted = old - new
                val added = new - old
                val updatedEntries = entries.filter { !deleted.contains(it.item) } +
                        added.map { itemStateOf(it, this) }.toList()
                entries = updatedEntries.sorted()
                entries.filterIsInstance<Expandable<*>>().filter { it.isExpanded }.forEach { it.reloadEntries() }
            }
        }
    }


    class NavigatorState<T : Navigable.Item<T>> internal constructor(container: Navigable<T>) {

        var minWidth by mutableStateOf(0.dp)
        val entries: List<ItemState<T>> by mutableStateOf(container.entries.map { itemStateOf(it, null) })
        val buttons: List<Form.ButtonArgs> = listOf(
            Form.ButtonArgs(Icon.Code.CHEVRONS_DOWN) { /*expand()*/ },
            Form.ButtonArgs(Icon.Code.CHEVRONS_UP) { /*collapse()*/ }
        )
    }

    @Composable
    fun <T : Navigable.Item<T>> rememberNavigatorState(navigable: Navigable<T>): NavigatorState<T> {
        return remember { NavigatorState(navigable) }
    }

    @Composable
    fun <T : Navigable.Item<T>> Layout(navigatorState: NavigatorState<T>) {

    }
}
