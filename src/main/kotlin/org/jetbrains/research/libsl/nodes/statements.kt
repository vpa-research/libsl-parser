package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.ActionReference
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition

sealed class Statement : Node()

data class Assignment(
    val left: QualifiedAccess,
    val op: AssignOps,
    val value: Expression,
    val entityPosition: EntityPosition
) : Statement() {
    override fun dumpToString(): String = "${left.dumpToString()} ${op.string} ${value.dumpToString()};"
}

data class IfStatement(
    val value: Expression,
    val ifStatements: MutableList<Statement> = mutableListOf(),
    val elseStatements: ElseStatement?,
    val entityPosition: EntityPosition
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
    val entityPosition: EntityPosition
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("else")
        appendLine(" {")
        append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
        appendLine("}")
    }
}

data class ActionUsage(
    val actionReference: ActionReference,
    val arguments: List<Expression>,
    val entityPosition: EntityPosition
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
    //val procReference: FunctionReference,
    val name: String,
    val arguments: List<Expression>,
    val entityPosition: EntityPosition
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("${BackticksPolitics.forIdentifier(name)}(")
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

data class FunctionUsage(
    val functionReference: FunctionReference,
    val arguments: List<Expression>,
    val entityPosition: EntityPosition
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("${BackticksPolitics.forIdentifier(functionReference.resolveOrError().name)}(")
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
    val expression: Expression,
    val entityPosition: EntityPosition
) : Statement() {
    override fun dumpToString(): String = buildString {
        append(expression.dumpToString())
        append(";")
    }
}

data class VariableDeclaration(
    val variable: VariableWithInitialValue,
    val entityPosition: EntityPosition
) : Statement() {
    override fun dumpToString(): String {
        return variable.dumpToString()
    }
}
