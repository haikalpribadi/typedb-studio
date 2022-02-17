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

package com.vaticle.typedb.studio.view.page

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Press
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaticle.typedb.studio.state.GlobalState
import com.vaticle.typedb.studio.state.page.Pageable
import com.vaticle.typedb.studio.view.common.KeyMapper
import com.vaticle.typedb.studio.view.common.Label
import com.vaticle.typedb.studio.view.common.Sentence
import com.vaticle.typedb.studio.view.common.component.Form.IconButton
import com.vaticle.typedb.studio.view.common.component.Form.Text
import com.vaticle.typedb.studio.view.common.component.Icon
import com.vaticle.typedb.studio.view.common.component.Separator
import com.vaticle.typedb.studio.view.common.theme.Theme
import com.vaticle.typedb.studio.view.common.theme.Theme.toDP
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object PageArea {

    val MIN_WIDTH = 300.dp
    private val TAB_SPACING = 8.dp
    private val TAB_HEIGHT = 28.dp
    private val TAB_UNDERLINE_HEIGHT = 2.dp
    private val ICON_SIZE = 10.sp

    internal class AreaState(val coroutineScope: CoroutineScope) {
        var density: Float by mutableStateOf(0f)
        val scrollState = ScrollState(0)
        var scrollTabsToEnd by mutableStateOf(false)
        val cachedPages: MutableMap<Pageable, Page> = mutableMapOf()

        fun handleKeyEvent(event: KeyEvent): Boolean {
            return if (event.type == KeyEventType.KeyUp) false
            else KeyMapper.CURRENT.map(event)?.let { execute(it) } ?: false
        }

        private fun execute(command: KeyMapper.Command): Boolean {
            return when (command) {
                KeyMapper.Command.NEW_PAGE -> createAndOpenNewFile()
                KeyMapper.Command.CLOSE -> closeSelectedPage()
                else -> false
            }
        }

        internal fun scrollBy(dp: Dp) {
            val pos = scrollState.value + (dp.value * density).toInt()
            coroutineScope.launch { scrollState.animateScrollTo(pos) }
        }

        internal fun createAndOpenNewFile(): Boolean {
            GlobalState.project.tryCreateUntitledFile()?.let { GlobalState.page.open(it) }
            scrollTabsToEnd = true
            return true
        }

        private fun closeSelectedPage(): Boolean {
            return GlobalState.page.selectedPage?.let { closePage(it) } ?: false
        }

        internal fun closePage(pageable: Pageable): Boolean {
            pageable.onClosePage?.let { it() }
            fun close() {
                cachedPages.remove(pageable)
                GlobalState.page.close(pageable)
                if (pageable.isUnsavedFile) pageable.delete()
            }
            if (pageable.isUnsaved) {
                GlobalState.confirmation.submit(
                    title = Label.SAVE_OR_DELETE,
                    message = Sentence.SAVE_OR_DELETE_FILE,
                    confirmLabel = Label.SAVE,
                    cancelLabel = Label.DELETE,
                    onCancel = { close() },
                    onConfirm = { pageable.saveFile() },
                )
            } else close()
            return true
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Area() {
        val density = LocalDensity.current.density
        val coroutineScope = rememberCoroutineScope()
        val state = remember { AreaState(coroutineScope) }
        state.density = density
        val focusReq = FocusRequester()
        fun mayRequestFocus() {
            if (GlobalState.page.openedPages.isEmpty()) focusReq.requestFocus()
        }
        (state.cachedPages.keys - GlobalState.page.openedPages.toSet()).forEach { state.cachedPages.remove(it) }
        state.cachedPages.values.forEach { it.resetFocus() }
        Column(
            modifier = Modifier.fillMaxSize().focusRequester(focusReq).focusable()
                .onPointerEvent(Press) { if (it.buttons.isPrimaryPressed) mayRequestFocus() }
                .onKeyEvent { state.handleKeyEvent(it) }
        ) {
            TabArea(state, density)
            Separator.Horizontal()
            GlobalState.page.selectedPage?.let { state.cachedPages[it]?.Layout() }
        }
        LaunchedEffect(focusReq) { mayRequestFocus() }
    }

    @Composable
    private fun TabArea(state: AreaState, density: Float) {
        var maxWidth by remember { mutableStateOf(4096.dp) }
        val scrollState = state.scrollState
        Row(Modifier.fillMaxWidth().height(TAB_HEIGHT).onSizeChanged { maxWidth = toDP(it.width, density) }) {
            if (scrollState.maxValue > 0) {
                PreviousTabsButton(state)
                Separator.Vertical()
            }
            val tabRowWidth = maxWidth - TAB_HEIGHT * 3
            Row(Modifier.widthIn(max = tabRowWidth).height(TAB_HEIGHT).horizontalScroll(scrollState)) {
                GlobalState.page.openedPages.forEach {
                    Tab(state, state.cachedPages.getOrPut(it) { Page.of(it) }, density)
                }
            }
            if (scrollState.maxValue > 0) {
                if (scrollState.value < scrollState.maxValue) Separator.Vertical()
                NextTabsButton(state)
                Separator.Vertical()
            }
            NewPageButton(state)
            Separator.Vertical()
        }
        LaunchedEffect(state.scrollTabsToEnd) {
            if (state.scrollTabsToEnd) {
                scrollState.animateScrollTo(scrollState.maxValue)
                state.scrollTabsToEnd = false
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun Tab(areaState: AreaState, page: Page, density: Float) {
        val isSelected = GlobalState.page.isSelected(page.state)
        val bgColor = if (isSelected) Theme.colors.primary else Theme.colors.background
        val height = if (isSelected) TAB_HEIGHT - TAB_UNDERLINE_HEIGHT else TAB_HEIGHT
        var width by remember { mutableStateOf(0.dp) }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(height)
                    .background(color = bgColor)
                    .pointerHoverIcon(PointerIconDefaults.Hand)
                    .clickable { GlobalState.page.select(page.state) }
                    .onSizeChanged { width = toDP(it.width, density) }
            ) {
                Spacer(modifier = Modifier.width(TAB_SPACING))
                Icon.Render(icon = page.icon.code, size = ICON_SIZE, color = page.icon.color())
                Spacer(modifier = Modifier.width(TAB_SPACING))
                Text(value = tabTitle(page))
                IconButton(
                    icon = Icon.Code.XMARK,
                    onClick = { areaState.closePage(page.state) },
                    modifier = Modifier.size(TAB_HEIGHT),
                    bgColor = Color.Transparent,
                    rounded = false,
                )
            }
            if (isSelected) Separator.Horizontal(TAB_UNDERLINE_HEIGHT, Theme.colors.secondary, Modifier.width(width))
        }
        Separator.Vertical()
    }

    @Composable
    private fun PreviousTabsButton(state: AreaState) {
        IconButton(
            icon = Icon.Code.CARET_LEFT,
            onClick = { state.scrollBy((-200).dp) },
            modifier = Modifier.size(TAB_HEIGHT),
            bgColor = Color.Transparent,
            rounded = false,
            enabled = state.scrollState.value > 0
        )
    }

    @Composable
    private fun NextTabsButton(state: AreaState) {
        IconButton(
            icon = Icon.Code.CARET_RIGHT,
            onClick = { state.scrollBy(200.dp) },
            modifier = Modifier.size(TAB_HEIGHT),
            bgColor = Color.Transparent,
            rounded = false,
            enabled = state.scrollState.value < state.scrollState.maxValue
        )
    }

    @Composable
    private fun NewPageButton(state: AreaState) {
        IconButton(
            icon = Icon.Code.PLUS,
            onClick = { state.createAndOpenNewFile() },
            modifier = Modifier.size(TAB_HEIGHT),
            bgColor = Color.Transparent,
            rounded = false,
            enabled = GlobalState.project.current != null
        )
    }

    @Composable
    private fun tabTitle(page: Page): AnnotatedString {
        return if (page.isWritable) {
            val changeIndicator = " *"
            AnnotatedString(page.label) + when {
                page.state.isUnsaved -> AnnotatedString(changeIndicator)
                else -> AnnotatedString(changeIndicator, SpanStyle(color = Color.Transparent))
            }
        } else {
            val builder = AnnotatedString.Builder()
            val style = SpanStyle(color = Theme.colors.onPrimary.copy(alpha = 0.6f))
            builder.append(page.label)
            builder.pushStyle(style)
            builder.append(" -- (${Label.READ_ONLY.lowercase()})")
            builder.pop()
            builder.toAnnotatedString()
        }
    }
}
