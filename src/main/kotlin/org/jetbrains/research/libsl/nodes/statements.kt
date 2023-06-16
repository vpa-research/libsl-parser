package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.Position

sealed class Statement: Node()

data class Assignment(
    val left: QualifiedAccess,
    val op: AssignOps,
    val value: Expression,
    val position: Position
) : Statement() {
    override fun dumpToString(): String = "${left.dumpToString()} ${op.string} ${value.dumpToString()};"
}

data class IfStatement(
    val value: Expression,
    val ifStatements: MutableList<Statement> = mutableListOf(),
    val elseStatements: ElseStatement?,
    val position: Position
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("if (${value.dumpToString()}) ")
        appendLine("{")
        append(withIndent(formatListEmptyLineAtEndIfNeeded(ifStatements)))
        appendLine("}")
        if(elseStatements?.statements?.isNotEmpty() == true) {
            append(elseStatements.dumpToString())
        }
    }
}

data class ElseStatement(
    val statements: MutableList<Statement> = mutableListOf(),
    val position: Position
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("else")
        appendLine(" {")
        append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
        appendLine("}")
    }
}

data class Action(
    val name: String,
    val arguments: MutableList<Expression> = mutableListOf(),
    val position: Position
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("action ${BackticksPolitics.forIdentifier(name)}(")
        if(arguments.isNotEmpty()) {
            val args = arguments.map { it.dumpToString() }.toMutableList()
            append(args.joinToString(separator = ", "))
        }
        append(");")
    }
}

data class ProcedureCall(
    val name: String,
    val arguments: MutableList<Expression> = mutableListOf(),
    val position: Position
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("${BackticksPolitics.forIdentifier(name)}(")
        if(arguments.isNotEmpty()) {
            val args = arguments.map { it.dumpToString() }
            append(args.joinToString(separator = ", "))
        }
        append(");")
    }
}

data class ExpressionStatement(
    val expression: Expression,
    val position: Position
) : Statement() {
    override fun dumpToString(): String = buildString {
        append(expression.dumpToString())
        append(";")
    }
}

data class VariableDeclaration(
    val variable: VariableWithInitialValue,
    val position: Position
) : Statement() {
    override fun dumpToString(): String {
        return variable.dumpToString()
    }
}
