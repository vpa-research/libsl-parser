package org.jetbrains.research.libsl.utils

import org.antlr.v4.runtime.ParserRuleContext
import org.jetbrains.research.libsl.visitors.position

data class Position(
    val fileName: String,
    val startPosition: StartPosition,
    val endPosition: EndPosition
    )

class PositionGetter {
    fun getCtxPosition(fileName: String, ctx: ParserRuleContext): Position {
        val startPosition = StartPosition(ctx.start.position().first, ctx.start.position().second)
        val endPosition = EndPosition(ctx.stop.position().first, ctx.stop.position().second)
        return Position(fileName, startPosition, endPosition)
    }
}

data class StartPosition(
    val line: Int,
    val column: Int
)

data class EndPosition(
    val line: Int,
    val column: Int
)
