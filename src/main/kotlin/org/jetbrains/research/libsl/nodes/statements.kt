package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.ActionDeclReference
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class Statement : Node()

data class Assignment(
    val left: QualifiedAccess,
    val op: AssignOps,
    val value: Expression
) : Statement() {
    override fun dumpToString(): String = "${left.dumpToString()} ${op.string} ${value.dumpToString()};"
}

data class IfStatement(
    val value: Expression,
    val ifStatements: MutableList<Statement> = mutableListOf(),
    val elseStatements: ElseStatement?
) : Statement() {
    override fun dumpToString(): String = buildString {
        if(ifStatements.size == 1) {
            appendLine("if (${value.dumpToString()}) ")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(ifStatements)))
        } else {
            appendLine("if (${value.dumpToString()}) {")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(ifStatements)))
            appendLine("}")
        }
        if(elseStatements?.statements?.isNotEmpty() == true) {
            append(elseStatements.dumpToString())
        }
    }
}

data class ElseStatement(
    val statements: MutableList<Statement> = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        if(statements.size == 1) {
            appendLine("else")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
        } else {
            appendLine("else {")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }
}

data class ActionUsage(
    val actionReference: ActionDeclReference,
    val arguments: List<Expression>
) : Statement() {
    override fun dumpToString(): String = buildString {
        append(BackticksPolitics.forIdentifier(actionReference.resolveOrError().name))
        if (arguments.isNotEmpty()) {
            append(
                arguments.joinToString(
                    prefix = "(",
                    separator = ", ",
                    postfix = ")",
                    transform = Expression::dumpToString
                )
            )
        }
    }
}

data class ProcedureCall(
    val procReference: FunctionReference,
    val arguments: List<Expression>
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("${BackticksPolitics.forIdentifier(procReference.resolveOrError().name)}(")
        if (arguments.isNotEmpty()) {
            append(
                arguments.joinToString(
                    prefix = "(",
                    separator = ", ",
                    postfix = ")",
                    transform = Expression::dumpToString
                )
            )
        }
    }
}

data class ExpressionStatement(
    val expression: Expression
) : Statement() {
    override fun dumpToString(): String = buildString {
        append(expression.dumpToString())
        append(";")
    }
}

data class VariableDeclaration(
    val variable: VariableWithInitialValue
) : Statement() {
    override fun dumpToString(): String {
        return variable.dumpToString()
    }
}
