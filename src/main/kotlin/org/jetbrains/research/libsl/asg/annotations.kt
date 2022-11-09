package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.visitors.addBacktickIfNeeded

open class Annotation(
    val name: String,
    val values: MutableList<Expression> = mutableListOf()
) : IPrinter {
    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("@${addBacktickIfNeeded(name)}")
        if (values.isNotEmpty()) {
            append(values.joinToString(prefix = "(", postfix = ")", separator = ", ") { v -> v.dumpToString() })
        }

        appendLine()
    }
}

class TargetAnnotation(
    name: String,
    values: MutableList<Expression>,
    val targetAutomaton: Automaton
) : Annotation(name, values) {
    override fun toString(): String = dumpToString()
}