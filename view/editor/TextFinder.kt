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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vaticle.typedb.studio.state.project.File
import com.vaticle.typedb.studio.view.common.Label
import com.vaticle.typedb.studio.view.editor.InputTarget.Cursor
import com.vaticle.typedb.studio.view.editor.InputTarget.Selection
import java.util.regex.MatchResult
import java.util.regex.Pattern
import kotlin.streams.toList

internal class TextFinder(private val file: File) {

    data class LineInfo(val start: Int, val length: Int)

    private var content: String by mutableStateOf("")
    private var lineInfo: List<LineInfo> by mutableStateOf(listOf())
    private var matches: List<Selection> by mutableStateOf(listOf())
    private var matchesByLine: Map<Int, List<Selection>> by mutableStateOf(mapOf())
    private var pattern: Pattern? by mutableStateOf(null)
    internal var position: Int by mutableStateOf(0)
    internal val hasMatches: Boolean get() = matches.isNotEmpty()

    internal fun status(): String {
        val count = if (matches.isNotEmpty()) "${position + 1} / ${matches.size}" else matches.size.toString()
        return "$count ${Label.FOUND.lowercase()}"
    }

    internal fun reset() {
        pattern = null
        position = 0
        matches = listOf()
        matchesByLine = mapOf()
    }

    internal fun mayRecompute(from: Cursor = Cursor(0, 0)) {
        pattern?.let {
            updateContent()
            computeMatches(index(from))
        }
    }

    internal fun updateContent() {
        val newLineInfo = mutableListOf<LineInfo>()
        file.content.forEachIndexed { i, line ->
            val length = if (i < file.content.size - 1) line.length + 1 else line.length
            if (i == 0) newLineInfo.add(LineInfo(0, length))
            else newLineInfo.add(LineInfo(newLineInfo[i - 1].start + newLineInfo[i - 1].length, length))
        }
        lineInfo = newLineInfo
        content = file.content.joinToString(separator = "\n")
    }

    internal fun findText(text: String, isCaseSensitive: Boolean) {
        findPattern(text, isCaseSensitive, false)
    }

    internal fun findWord(word: String, isCaseSensitive: Boolean) {
        findPattern("\\b$word\\b", isCaseSensitive, true)
    }

    internal fun findRegex(regex: String, isCaseSensitive: Boolean) {
        findPattern(regex, isCaseSensitive, true)
    }

    private fun findPattern(patternStr: String, isCaseSensitive: Boolean, isRegex: Boolean) {
        assert(patternStr.isNotEmpty())
        val caseFlag = if (isCaseSensitive) 0 else Pattern.CASE_INSENSITIVE
        val literalFlag = if (isRegex) 0 else Pattern.LITERAL
        try {
            pattern = Pattern.compile(patternStr, caseFlag or literalFlag)
            computeMatches(0)
            trySetPosition(0)
        } catch (e: Exception) {
            reset()
        }
    }

    private fun computeMatches(fromIndex: Int) {
        println("computeMatches() -> fromIndex: $fromIndex")
        val byLine = mutableMapOf<Int, MutableList<Selection>>()
        val matcher = pattern!!.matcher(content)
        matches = if (matcher.find(fromIndex)) matcher.results().map { selection(it) }.toList() else listOf()
        matches.forEach {
            (it.min.row..it.end.row).forEach { i -> byLine.computeIfAbsent(i) { mutableListOf() }.add(it) }
        }
        matchesByLine = byLine
    }

    private fun selection(match: MatchResult): Selection {
        return Selection(cursor(match.start()), cursor(match.end()))
    }

    private fun cursor(index: Int): Cursor {
        var row = 0
        var col = index

        while (col >= lineInfo[row].length) {
            col -= lineInfo[row].length
            row++
        }
        return Cursor(row, col)
    }

    private fun index(cursor: Cursor): Int {
        var index = 0
        (0..cursor.row).forEach { i ->
            index += when {
                i < cursor.row -> lineInfo[i].length
                else -> cursor.col
            }
            if (i > 0) index += 1
        }
        return index
    }

    internal fun trySetPosition(newPosition: Int): Int {
        position = newPosition.coerceIn(0, (matches.size - 1).coerceAtLeast(0))
        return position
    }

    internal fun findCurrent(): Selection? {
        return if (hasMatches) matches[position] else null
    }

    internal fun findFirst(): Selection? {
        return if (hasMatches) matches[trySetPosition(0)] else null
    }

    internal fun findNext(): Selection? {
        return if (hasMatches) matches[trySetPosition((position + 1) % matches.size)] else null
    }

    internal fun findPrevious(): Selection? {
        return if (hasMatches) {
            var newPos = position - 1
            if (newPos < 0) newPos += matches.size
            matches[trySetPosition(newPos)]
        } else null
    }

    internal fun matches(line: Int): List<Selection> {
        return matchesByLine[line] ?: listOf()
    }
}