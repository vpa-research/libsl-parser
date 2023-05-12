package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class Statement: Node()

data class Assignment(
    val left: QualifiedAccess,
    val value: Expression
) : Statement() {
    override fun dumpToString(): String = "${left.dumpToString()} = ${value.dumpToString()};"
}

data class AssignmentWithCompoundOp(
    val left: QualifiedAccess,
    val op: CompoundOps,
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
    val statements: MutableList<Statement> = mutableListOf()
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
    val arguments: MutableList<Expression>? = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("action ${BackticksPolitics.forIdentifier(name)}(")
        if(arguments?.isNotEmpty() == true) {
            val args = arguments.map { it.dumpToString() }.toMutableList()
            append(args.joinToString(separator = ", "))
        }
        append(");")
    }
}

data class ProcedureCall(
    val name: String,
    val arguments: MutableList<Expression>? = mutableListOf(),
    val hasThisExpression: Boolean
) : Statement() {
    override fun dumpToString(): String = buildString {
        if(hasThisExpression) {
            append("this.")
        }
        append("${BackticksPolitics.forIdentifier(name)}(")
        if(arguments?.isNotEmpty() == true) {
            val args = arguments.map { it.dumpToString() }.toMutableList()
            append(args.joinToString(separator = ", "))
        }
        append(");")
    }
}

data class ExpressionStatement(
    val expression: Expression
) : Statement() {
    override fun dumpToString(): String = buildString {
        expression.dumpToString()
    }
}

data class VariableDeclaration(
    val variable: VariableWithInitialValue
) : Statement() {
    override fun dumpToString(): String {
        return variable.dumpToString()
    }
}
