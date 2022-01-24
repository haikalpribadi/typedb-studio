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

package com.vaticle.typedb.studio.view.editor

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key

internal interface KeyMapping {
    fun map(event: KeyEvent): KeyCommand?
}

/**
 * Copied from [Key] as the constants there are experimental
 */
internal object MappedKeys {
    val A: Key = Key(java.awt.event.KeyEvent.VK_A)
    val B: Key = Key(java.awt.event.KeyEvent.VK_B)
    val D: Key = Key(java.awt.event.KeyEvent.VK_D)
    val C: Key = Key(java.awt.event.KeyEvent.VK_C)
    val E: Key = Key(java.awt.event.KeyEvent.VK_E)
    val F: Key = Key(java.awt.event.KeyEvent.VK_F)
    val H: Key = Key(java.awt.event.KeyEvent.VK_H)
    val K: Key = Key(java.awt.event.KeyEvent.VK_K)
    val N: Key = Key(java.awt.event.KeyEvent.VK_N)
    val O: Key = Key(java.awt.event.KeyEvent.VK_O)
    val P: Key = Key(java.awt.event.KeyEvent.VK_P)
    val V: Key = Key(java.awt.event.KeyEvent.VK_V)
    val X: Key = Key(java.awt.event.KeyEvent.VK_X)
    val Z: Key = Key(java.awt.event.KeyEvent.VK_Z)
    val Backslash: Key = Key(java.awt.event.KeyEvent.VK_BACK_SLASH)
    val DirectionLeft: Key = Key(java.awt.event.KeyEvent.VK_LEFT)
    val DirectionRight: Key = Key(java.awt.event.KeyEvent.VK_RIGHT)
    val DirectionUp: Key = Key(java.awt.event.KeyEvent.VK_UP)
    val DirectionDown: Key = Key(java.awt.event.KeyEvent.VK_DOWN)
    val PageUp: Key = Key(java.awt.event.KeyEvent.VK_PAGE_UP)
    val PageDown: Key = Key(java.awt.event.KeyEvent.VK_PAGE_DOWN)
    val MoveHome: Key = Key(java.awt.event.KeyEvent.VK_HOME)
    val MoveEnd: Key = Key(java.awt.event.KeyEvent.VK_END)
    val Insert: Key = Key(java.awt.event.KeyEvent.VK_INSERT)
    val Enter: Key = Key(java.awt.event.KeyEvent.VK_ENTER)
    val Backspace: Key = Key(java.awt.event.KeyEvent.VK_BACK_SPACE)
    val Delete: Key = Key(java.awt.event.KeyEvent.VK_DELETE)
    val Paste: Key = Key(java.awt.event.KeyEvent.VK_PASTE)
    val Cut: Key = Key(java.awt.event.KeyEvent.VK_CUT)
    val Copy: Key = Key(java.awt.event.KeyEvent.VK_COPY)
    val Tab: Key = Key(java.awt.event.KeyEvent.VK_TAB)
    val Space: Key = Key(java.awt.event.KeyEvent.VK_SPACE)
}

// It's common for all platforms key mapping
internal fun commonKeyMapping(shortcutModifier: (KeyEvent) -> Boolean): KeyMapping {
    return object : KeyMapping {
        override fun map(event: KeyEvent): KeyCommand? {
            return when {
                shortcutModifier(event) && event.isShiftPressed ->
                    when (event.key) {
                        MappedKeys.Z -> KeyCommand.REDO
                        else -> null
                    }
                shortcutModifier(event) ->
                    when (event.key) {
                        MappedKeys.C, MappedKeys.Insert -> KeyCommand.COPY
                        MappedKeys.V -> KeyCommand.PASTE
                        MappedKeys.X -> KeyCommand.CUT
                        MappedKeys.A -> KeyCommand.SELECT_ALL
                        MappedKeys.Z -> KeyCommand.UNDO
                        else -> null
                    }
                event.isCtrlPressed -> null
                event.isShiftPressed ->
                    when (event.key) {
                        MappedKeys.DirectionLeft -> KeyCommand.SELECT_LEFT_CHAR
                        MappedKeys.DirectionRight -> KeyCommand.SELECT_RIGHT_CHAR
                        MappedKeys.DirectionUp -> KeyCommand.SELECT_UP
                        MappedKeys.DirectionDown -> KeyCommand.SELECT_DOWN
                        MappedKeys.PageUp -> KeyCommand.SELECT_PAGE_UP
                        MappedKeys.PageDown -> KeyCommand.SELECT_PAGE_DOWN
                        MappedKeys.MoveHome -> KeyCommand.SELECT_LINE_START
                        MappedKeys.MoveEnd -> KeyCommand.SELECT_LINE_END
                        MappedKeys.Insert -> KeyCommand.PASTE
                        else -> null
                    }
                else ->
                    when (event.key) {
                        MappedKeys.DirectionLeft -> KeyCommand.LEFT_CHAR
                        MappedKeys.DirectionRight -> KeyCommand.RIGHT_CHAR
                        MappedKeys.DirectionUp -> KeyCommand.UP
                        MappedKeys.DirectionDown -> KeyCommand.DOWN
                        MappedKeys.PageUp -> KeyCommand.PAGE_UP
                        MappedKeys.PageDown -> KeyCommand.PAGE_DOWN
                        MappedKeys.MoveHome -> KeyCommand.LINE_START
                        MappedKeys.MoveEnd -> KeyCommand.LINE_END
                        MappedKeys.Enter -> KeyCommand.NEW_LINE
                        MappedKeys.Backspace -> KeyCommand.DELETE_PREV_CHAR
                        MappedKeys.Delete -> KeyCommand.DELETE_NEXT_CHAR
                        MappedKeys.Paste -> KeyCommand.PASTE
                        MappedKeys.Cut -> KeyCommand.CUT
                        MappedKeys.Tab -> KeyCommand.TAB
                        else -> null
                    }
            }
        }
    }
}

// It's "default" or actually "non macOS" key mapping
internal val defaultKeyMapping: KeyMapping =
    commonKeyMapping(KeyEvent::isCtrlPressed).let { common ->
        object : KeyMapping {
            override fun map(event: KeyEvent): KeyCommand? {
                return when {
                    event.isShiftPressed && event.isCtrlPressed ->
                        when (event.key) {
                            MappedKeys.DirectionLeft -> KeyCommand.SELECT_LEFT_WORD
                            MappedKeys.DirectionRight -> KeyCommand.SELECT_RIGHT_WORD
                            MappedKeys.DirectionUp -> KeyCommand.SELECT_PREV_PARAGRAPH
                            MappedKeys.DirectionDown -> KeyCommand.SELECT_NEXT_PARAGRAPH
                            else -> null
                        }
                    event.isCtrlPressed ->
                        when (event.key) {
                            MappedKeys.DirectionLeft -> KeyCommand.LEFT_WORD
                            MappedKeys.DirectionRight -> KeyCommand.RIGHT_WORD
                            MappedKeys.DirectionUp -> KeyCommand.PREV_PARAGRAPH
                            MappedKeys.DirectionDown -> KeyCommand.NEXT_PARAGRAPH
                            MappedKeys.H -> KeyCommand.DELETE_PREV_CHAR
                            MappedKeys.Delete -> KeyCommand.DELETE_NEXT_WORD
                            MappedKeys.Backspace -> KeyCommand.DELETE_PREV_WORD
                            MappedKeys.Backslash -> KeyCommand.DESELECT
                            else -> null
                        }
                    event.isShiftPressed ->
                        when (event.key) {
                            MappedKeys.MoveHome -> KeyCommand.SELECT_HOME
                            MappedKeys.MoveEnd -> KeyCommand.SELECT_END
                            else -> null
                        }
                    else -> null
                } ?: common.map(event)
            }
        }
    }

internal val platformDefaultKeyMapping: KeyMapping =
    when (DesktopPlatform.Current) {
        DesktopPlatform.MacOS -> {
            val common = commonKeyMapping(KeyEvent::isMetaPressed)
            object : KeyMapping {
                override fun map(event: KeyEvent): KeyCommand? {
                    return when {
                        event.isMetaPressed && event.isCtrlPressed ->
                            when (event.key) {
                                MappedKeys.Space -> KeyCommand.CHARACTER_PALETTE
                                else -> null
                            }
                        event.isShiftPressed && event.isAltPressed ->
                            when (event.key) {
                                MappedKeys.DirectionLeft -> KeyCommand.SELECT_LEFT_WORD
                                MappedKeys.DirectionRight -> KeyCommand.SELECT_RIGHT_WORD
                                MappedKeys.DirectionUp -> KeyCommand.SELECT_PREV_PARAGRAPH
                                MappedKeys.DirectionDown -> KeyCommand.SELECT_NEXT_PARAGRAPH
                                else -> null
                            }
                        event.isShiftPressed && event.isMetaPressed ->
                            when (event.key) {
                                MappedKeys.DirectionLeft -> KeyCommand.SELECT_LINE_LEFT
                                MappedKeys.DirectionRight -> KeyCommand.SELECT_LINE_RIGHT
                                MappedKeys.DirectionUp -> KeyCommand.SELECT_HOME
                                MappedKeys.DirectionDown -> KeyCommand.SELECT_END
                                else -> null
                            }

                        event.isMetaPressed ->
                            when (event.key) {
                                MappedKeys.DirectionLeft -> KeyCommand.LINE_LEFT
                                MappedKeys.DirectionRight -> KeyCommand.LINE_RIGHT
                                MappedKeys.DirectionUp -> KeyCommand.HOME
                                MappedKeys.DirectionDown -> KeyCommand.END
                                MappedKeys.Backspace -> KeyCommand.DELETE_FROM_LINE_START
                                else -> null
                            }

                        // Emacs-like shortcuts
                        event.isCtrlPressed && event.isShiftPressed && event.isAltPressed -> {
                            when (event.key) {
                                MappedKeys.F -> KeyCommand.SELECT_RIGHT_WORD
                                MappedKeys.B -> KeyCommand.SELECT_LEFT_WORD
                                else -> null
                            }
                        }
                        event.isCtrlPressed && event.isAltPressed -> {
                            when (event.key) {
                                MappedKeys.F -> KeyCommand.RIGHT_WORD
                                MappedKeys.B -> KeyCommand.LEFT_WORD
                                else -> null
                            }
                        }
                        event.isCtrlPressed && event.isShiftPressed -> {
                            when (event.key) {
                                MappedKeys.F -> KeyCommand.SELECT_RIGHT_CHAR
                                MappedKeys.B -> KeyCommand.SELECT_LEFT_CHAR
                                MappedKeys.P -> KeyCommand.SELECT_UP
                                MappedKeys.N -> KeyCommand.SELECT_DOWN
                                MappedKeys.A -> KeyCommand.SELECT_LINE_START
                                MappedKeys.E -> KeyCommand.SELECT_LINE_END
                                else -> null
                            }
                        }
                        event.isCtrlPressed -> {
                            when (event.key) {
                                MappedKeys.F -> KeyCommand.LEFT_CHAR
                                MappedKeys.B -> KeyCommand.RIGHT_CHAR
                                MappedKeys.P -> KeyCommand.UP
                                MappedKeys.N -> KeyCommand.DOWN
                                MappedKeys.A -> KeyCommand.LINE_START
                                MappedKeys.E -> KeyCommand.LINE_END
                                MappedKeys.H -> KeyCommand.DELETE_PREV_CHAR
                                MappedKeys.D -> KeyCommand.DELETE_NEXT_CHAR
                                MappedKeys.K -> KeyCommand.DELETE_TO_LINE_END
                                MappedKeys.O -> KeyCommand.NEW_LINE
                                else -> null
                            }
                        }
                        // end of emacs-like shortcuts

                        event.isShiftPressed ->
                            when (event.key) {
                                MappedKeys.MoveHome -> KeyCommand.SELECT_HOME
                                MappedKeys.MoveEnd -> KeyCommand.SELECT_END
                                else -> null
                            }
                        event.isAltPressed ->
                            when (event.key) {
                                MappedKeys.DirectionLeft -> KeyCommand.LEFT_WORD
                                MappedKeys.DirectionRight -> KeyCommand.RIGHT_WORD
                                MappedKeys.DirectionUp -> KeyCommand.PREV_PARAGRAPH
                                MappedKeys.DirectionDown -> KeyCommand.NEXT_PARAGRAPH
                                MappedKeys.Delete -> KeyCommand.DELETE_NEXT_WORD
                                MappedKeys.Backspace -> KeyCommand.DELETE_PREV_WORD
                                else -> null
                            }
                        else -> null
                    } ?: common.map(event)
                }
            }
        }

        else -> defaultKeyMapping
    }