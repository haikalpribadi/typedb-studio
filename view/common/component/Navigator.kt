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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.state.State
import com.vaticle.typedb.studio.state.common.Navigable
import com.vaticle.typedb.studio.state.notification.Error
import com.vaticle.typedb.studio.state.notification.Message
import com.vaticle.typedb.studio.view.common.theme.Theme
import com.vaticle.typedb.studio.view.common.theme.Theme.toDP
import java.awt.event.KeyEvent.KEY_RELEASED
import java.util.*
import mu.KotlinLogging

class Navigator<T : Navigable.Item<T>>(
    private val name: String,
    private val navigable: Navigable<T>,
    private val iconArgs: (T) -> IconArgs,
    private val itemHeight: Dp = ITEM_HEIGHT,
    private val reloadOnExpand: Boolean = true,
    private val contextMenuFn: ((ItemState<T>) -> List<ContextMenu.Item>)? = null,
    private val onOpen: (ItemState<T>) -> Unit
) {

    companion object {
        private const val MAX_ITEM_EXPANDED = 512
        private val ITEM_HEIGHT = 26.dp
        private val ICON_WIDTH = 20.dp
        private val TEXT_SPACING = 4.dp
        private val AREA_PADDING = 8.dp
        private val LOGGER = KotlinLogging.logger {}
    }

    data class IconArgs(val code: Icon.Code, val color: @Composable () -> Color = { Theme.colors.icon })

    class ItemState<T : Navigable.Item<T>> constructor(
        val value: T, val container: ItemState<T>?, private val reloadOnExpand: Boolean
    ) {

        private val hash = Objects.hash(value, reloadOnExpand)
        var focusRequester: FocusRequester? = null
        val isExpandable: Boolean get() = value.isContainer
        val isExpanded: Boolean get() = value.isContainer && value.asContainer().isNavigated.value
        val entries: List<ItemState<T>> =
            if (value.isContainer) value.asContainer().entries.value.map { ItemState(it, this, reloadOnExpand) }
            else emptyList()

        fun toggle() {
            toggle(!isExpanded)
        }

        fun expand() {
            toggle(true)
        }

        fun collapse() {
            toggle(false)
        }

        fun toggle(isExpanded: Boolean) {
            if (!isExpandable) throw IllegalStateException("Cannot toggle an item that is not expandable")
            value.asContainer().isNavigated.value = isExpanded
            if (reloadOnExpand && isExpanded) value.asContainer().reloadEntries()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ItemState<*>
            return value != other.value && reloadOnExpand != other.reloadOnExpand
        }

        override fun hashCode(): Int {
            return hash
        }
    }

    private var minWidth by mutableStateOf(0.dp)
    private var selected: ItemState<T>? by mutableStateOf(null)
    val entries: List<ItemState<T>> = navigable.entries.value.map { ItemState(it, null, reloadOnExpand) }
    val buttons: List<Form.ButtonArgs> = listOf(
        Form.ButtonArgs(Icon.Code.CHEVRONS_DOWN) { expand() },
        Form.ButtonArgs(Icon.Code.CHEVRONS_UP) { collapse() }
    )

    private fun expand() {
        if (!toggle(true)) {
            val error = Error.fromUser(Message.View.CATALOG_EXPAND_LIMIT, name, MAX_ITEM_EXPANDED)
            State.notification.userError(error, LOGGER)
        }
    }

    private fun collapse() {
        toggle(false)
    }

    private fun toggle(isExpanded: Boolean): Boolean {
        var i = 0
        val queue: LinkedList<ItemState<T>> = LinkedList(entries.filter { it.isExpandable })

        while (queue.isNotEmpty() && i < MAX_ITEM_EXPANDED) {
            val item = queue.pop()
            item.toggle(isExpanded)
            if (isExpanded) {
                i += item.entries.count()
                queue.addAll(item.entries.filter { it.isExpandable })
            } else {
                queue.addAll(item.entries.filter { it.isExpanded })
            }
        }
        return queue.isEmpty()
    }

    private fun open(item: ItemState<T>) {
        onOpen(item)
    }

    private fun select(item: ItemState<T>) {
        println("Select: ${item.value}")
        selected = item
        item.focusRequester?.requestFocus()
    }

    private fun selectNext(item: ItemState<T>) {
        println("Select next from: ${item.value}") // TODO
    }

    private fun selectPrevious(item: ItemState<T>) {
        println("Select previous from: ${item.value}") // TODO
    }

    private fun selectContainer(item: ItemState<T>) {
        item.container?.let { select(it) }
    }

    @Composable
    fun Layout() {
        val density = LocalDensity.current.density
        Box(
            modifier = Modifier.fillMaxSize()
                .onSizeChanged { minWidth = toDP(it.width, density) }
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        ) { NestedCatalog(0, entries, iconArgs, itemHeight, contextMenuFn) }
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
    @Composable
    private fun NestedCatalog(
        depth: Int, items: List<ItemState<T>>, iconArgs: (T) -> IconArgs, itemHeight: Dp,
        contextMenuFn: ((ItemState<T>) -> List<ContextMenu.Item>)?
    ) {
        val density = LocalDensity.current.density
        fun increaseToAtLeast(widthSize: Int) {
            val newWidth = toDP(widthSize, density)
            if (newWidth > minWidth) minWidth = newWidth
        }
        Column(modifier = Modifier.widthIn(min = minWidth).onSizeChanged { increaseToAtLeast(it.width) }) {
            items.forEach { item ->
                ContextMenu.Area(contextMenuFn?.let { { it(item) } }, { select(item) }) {
                    ItemLayout(depth, item, iconArgs, itemHeight) { increaseToAtLeast(it) }
                }
                if (item.isExpanded) NestedCatalog(depth + 1, item.entries, iconArgs, itemHeight, contextMenuFn)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun ItemLayout(
        depth: Int, item: ItemState<T>, iconArgs: (T) -> IconArgs, itemHeight: Dp, onSizeChanged: (Int) -> Unit
    ) {
        val focusReq = remember { FocusRequester() }.also { item.focusRequester = it }
        val bgColor = when {
            selected == item -> Theme.colors.error
            else -> Color.Transparent
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(color = bgColor)
                .widthIn(min = minWidth).height(itemHeight)
                .onSizeChanged { onSizeChanged(it.width) }
                .focusRequester(focusReq)
                .onKeyEvent { onKeyEvent(it, item) }
                .pointerHoverIcon(PointerIconDefaults.Hand)
                .onPointerEvent(PointerEventType.Press) { onPointerEvent(it, focusReq, item) }
                .clickable { }
        ) {
            if (depth > 0) Spacer(modifier = Modifier.width(ICON_WIDTH * depth))
            ItemButton(item, itemHeight)
            ItemIcon(item, iconArgs)
            Spacer(Modifier.width(TEXT_SPACING))
            ItemText(item)
            Spacer(modifier = Modifier.width(AREA_PADDING))
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun ItemButton(item: ItemState<T>, size: Dp) {
        if (item.isExpandable) Form.RawClickableIcon(
            icon = if (item.isExpanded) Icon.Code.CHEVRON_DOWN else Icon.Code.CHEVRON_RIGHT,
            onClick = { item.toggle() },
            modifier = Modifier.size(size)
        ) else Spacer(Modifier.size(size))
    }

    @Composable
    private fun ItemIcon(item: ItemState<T>, iconArgs: (T) -> IconArgs) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ICON_WIDTH)) {
            Icon.Render(icon = iconArgs(item.value).code, color = iconArgs(item.value).color())
        }
    }

    @Composable
    private fun ItemText(item: ItemState<T>) {
        Row(modifier = Modifier.height(ICON_WIDTH)) {
            Form.Text(value = item.value.name)
            item.value.info?.let {
                Spacer(Modifier.width(TEXT_SPACING))
                Form.Text(value = "( $it )", alpha = 0.4f)
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun onKeyEvent(event: KeyEvent, item: ItemState<T>): Boolean {
        return when (event.awtEvent.id) {
            KEY_RELEASED -> false
            else -> when (event.key) {
                Key.Enter, Key.NumPadEnter -> {
                    if (selected == item) open(item)
                    else select(item)
                    true
                }
                Key.DirectionLeft -> {
                    if (item.isExpanded) item.collapse()
                    else selectContainer(item)
                    true
                }
                Key.DirectionRight -> {
                    if (item.isExpandable && !item.isExpanded) item.expand()
                    else selectNext(item)
                    true
                }
                Key.DirectionUp -> {
                    selectPrevious(item)
                    true
                }
                Key.DirectionDown -> {
                    selectNext(item)
                    true
                }
                else -> false
            }
        }
    }

    private fun onPointerEvent(event: PointerEvent, focusReq: FocusRequester, item: ItemState<T>) {
        when {
            event.buttons.isPrimaryPressed -> when (event.awtEvent.clickCount) {
                1 -> {
                    select(item)
                    focusReq.requestFocus()
                }
                2 -> open(item)
            }
        }
    }
}
