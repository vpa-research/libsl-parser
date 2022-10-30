package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.visitors.addBacktickIfNeeded

sealed class Statement: Node()
data class Assignment(
    val left: QualifiedAccess,
    val value: Expression
) : Statement() {
    override fun dumpToString(): String = "${left.dumpToString()} = ${value.dumpToString()};"
}

data class Action(
    val name: String,
    val arguments: MutableList<Expression> = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("action ${addBacktickIfNeeded(name)}(")
        val args = arguments.map { it.dumpToString() }.toMutableList()
        append(args.joinToString(separator = ", "))
        append(");")
    }
}