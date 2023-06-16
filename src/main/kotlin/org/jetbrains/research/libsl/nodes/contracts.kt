package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.Position

data class Contract(
    val name: String?,
    val expression: Expression,
    val kind: ContractKind,
    val position: Position
) : Node() {
    override fun dumpToString(): String = buildString {
        append(kind.keyword)
        append(IPrinter.SPACE)
        if (name != null) {
            append(BackticksPolitics.forIdentifier(name))
            append(": ")
        }
        append(expression.dumpToString())
        append(";")
    }
}

enum class ContractKind(val keyword: String) {
    REQUIRES("requires"), ENSURES("ensures"), ASSIGNS("assigns")
}
