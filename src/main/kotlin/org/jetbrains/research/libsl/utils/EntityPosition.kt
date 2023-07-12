package org.jetbrains.research.libsl.utils

import org.antlr.v4.runtime.ParserRuleContext
import org.jetbrains.research.libsl.visitors.position

data class EntityPosition(
    val fileName: String,
    val startPosition: PositionInfo,
    val endPosition: PositionInfo
)

val EntityPosition.string: String
    get() = "$fileName.lsl, start position[${startPosition.line}:${startPosition.column}], end position[${endPosition.line}:${endPosition.column}]"

class PositionGetter {
    fun getCtxPosition(fileName: String, ctx: ParserRuleContext): EntityPosition {
        val startPosition = PositionInfo(ctx.start.position().first, ctx.start.position().second)
        val endPosition = PositionInfo(ctx.stop.position().first, ctx.stop.position().second)
        return EntityPosition(fileName, startPosition, endPosition)
    }
}

data class PositionInfo(
    val line: Int,
    val column: Int
)
