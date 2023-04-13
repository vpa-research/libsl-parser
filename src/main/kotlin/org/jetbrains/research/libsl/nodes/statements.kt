package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class Statement: Node()
data class Assignment(
    val left: QualifiedAccess,
    val value: Expression
) : Statement() {
    override fun dumpToString(): String = "${left.dumpToString()} = ${value.dumpToString()};"
}

data class IfStatement(
    val value: Expression,
    val ifStatements: MutableList<Statement> = mutableListOf(),
    val elseStatements: ElseStatement?
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("if${value.dumpToString()}")
        appendLine(" {")
        append(withIndent(formatListEmptyLineAtEndIfNeeded(ifStatements)))
        appendLine("}")
        elseStatements?.also {
            append("else")
            appendLine(" {")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(it.statements)))
            appendLine("}")
        }
    }
}

data class ElseStatement(
    val statements: MutableList<Statement> = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
    }

}

data class Action(
    val name: String,
    val arguments: MutableList<Expression> = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("action ${BackticksPolitics.forIdentifier(name)}(")
        val args = arguments.map { it.dumpToString() }.toMutableList()
        append(args.joinToString(separator = ", "))
        append(");")
    }
}

data class ExpressionStatement(
    val expressions: MutableList<Expression> = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        expressions.forEach { e -> e.dumpToString() }
    }

}
