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

package com.vaticle.typedb.studio.view.highlighter

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Builder
import androidx.compose.ui.text.SpanStyle
import com.vaticle.typedb.studio.state.common.Property
import com.vaticle.typedb.studio.view.highlighter.common.Lexer
import com.vaticle.typedb.studio.view.highlighter.common.Lexer.Token
import com.vaticle.typedb.studio.view.highlighter.common.Scheme
import com.vaticle.typedb.studio.view.highlighter.language.TypeQLLexer

object SyntaxHighlighter {

    fun highlight(texts: List<String>, fileType: Property.FileType): List<AnnotatedString> {
        return texts.map { highlight(it, fileType) }
    }

    fun highlight(text: String, fileType: Property.FileType): AnnotatedString {
        return when (fileType) {
            Property.FileType.TYPEQL -> annotate(text, TypeQLLexer, Scheme.DRACULA)
            else -> AnnotatedString(text)
        }
    }

    private fun annotate(text: String, lexer: Lexer, scheme: Scheme): AnnotatedString {
        if (text.isBlank()) return AnnotatedString("")
        val builder = Builder()
        lexer.tokenize(text, scheme).forEach { builder.appendToken(it) }
        println()
        return builder.toAnnotatedString()
    }

    private fun Builder.appendToken(token: Token): Builder {
//        print("[${token.type}:${tokenName(token)}:${token.text}]")
        return this.appendText(token.text, Color.Green)
    }


    private fun Builder.appendText(text: String, color: Color): Builder {
        this.pushStyle(SpanStyle(color = color))
        this.append(text)
        this.pop()
        return this
    }

    private fun replaceSpace(text: String): String {
        return text.replace(" ", "_")
    }
}
