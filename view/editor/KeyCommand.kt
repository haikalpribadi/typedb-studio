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

internal enum class KeyCommand(
    // Indicates, that this command is supposed to edit text so should be applied only to
    // editable text fields
    val editsText: Boolean
) {
    LEFT_CHAR(false),
    RIGHT_CHAR(false),

    RIGHT_WORD(false),
    LEFT_WORD(false),

    NEXT_PARAGRAPH(false),
    PREV_PARAGRAPH(false),

    LINE_START(false),
    LINE_END(false),
    LINE_LEFT(false),
    LINE_RIGHT(false),

    UP(false),
    DOWN(false),

    PAGE_UP(false),
    PAGE_DOWN(false),

    HOME(false),
    END(false),

    COPY(false),
    PASTE(true),
    CUT(true),

    DELETE_PREV_CHAR(true),
    DELETE_NEXT_CHAR(true),

    DELETE_PREV_WORD(true),
    DELETE_NEXT_WORD(true),

    DELETE_FROM_LINE_START(true),
    DELETE_TO_LINE_END(true),

    SELECT_ALL(false),

    SELECT_LEFT_CHAR(false),
    SELECT_RIGHT_CHAR(false),

    SELECT_UP(false),
    SELECT_DOWN(false),

    SELECT_PAGE_UP(false),
    SELECT_PAGE_DOWN(false),

    SELECT_HOME(false),
    SELECT_END(false),

    SELECT_LEFT_WORD(false),
    SELECT_RIGHT_WORD(false),
    SELECT_NEXT_PARAGRAPH(false),
    SELECT_PREV_PARAGRAPH(false),

    SELECT_LINE_START(false),
    SELECT_LINE_END(false),
    SELECT_LINE_LEFT(false),
    SELECT_LINE_RIGHT(false),

    DESELECT(false),

    NEW_LINE(true),
    TAB(true),

    UNDO(true),
    REDO(true),
    CHARACTER_PALETTE(true)
}