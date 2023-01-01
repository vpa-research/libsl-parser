package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.utils.BackticksPolitics

open class Annotation(
    val name: String,
    val values: MutableList<Expression> = mutableListOf()
) : IPrinter {
    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("@${BackticksPolitics.forPeriodSeparated(name)}")
        if (values.isNotEmpty()) {
            append(values.joinToString(prefix = "(", postfix = ")", separator = ", ") { v -> v.dumpToString() })
        }

        appendLine()
    }
}

class TargetAnnotation(
) : Annotation("target", mutableListOf()) {
    override fun toString(): String = dumpToString()
}
