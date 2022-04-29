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

package com.vaticle.typedb.studio.state.connection

import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typedb.client.api.answer.ConceptMapGroup
import com.vaticle.typedb.client.api.answer.NumericGroup
import com.vaticle.typedb.common.collection.Either
import java.util.concurrent.LinkedBlockingQueue

sealed class Response {

    object Done : Response()

    data class Message(val type: Type, val text: String) : Response() {
        enum class Type { INFO, SUCCESS, ERROR, TYPEQL }
    }

    data class Numeric(val value: com.vaticle.typedb.client.api.answer.Numeric) : Response()

    sealed class Collector<T> : Response() {

        val queue = LinkedBlockingQueue<Either<T, Done>>()

        class ConceptMaps : Collector<ConceptMap>()
        class ConceptMapGroups : Collector<ConceptMapGroup>()
        class NumericGroups : Collector<NumericGroup>()
    }
}