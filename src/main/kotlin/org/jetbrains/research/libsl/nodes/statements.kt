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

data class AssignmentWithLeftUnaryOp(
    val op: ArithmeticUnaryOp,
    val value: Expression
) : Statement() {
    override fun dumpToString(): String = "${op.string}${value.dumpToString()};"
}

data class AssignmentWithRightUnaryOp(
    val op: ArithmeticUnaryOp,
    val value: Expression
) : Statement() {
    override fun dumpToString(): String = "${value.dumpToString()}${op.string};"
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
    val arguments: MutableList<Expression>? = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("action ${BackticksPolitics.forIdentifier(name)}(")
        if(arguments != null) {
            val args = arguments.map { it.dumpToString() }.toMutableList()
            append(args.joinToString(separator = ", "))
        }
        append(");")
    }
}

data class Proc(
    val name: String,
    val arguments: MutableList<Expression>? = mutableListOf(),
    val hasThisExpression: Boolean,
    val hasParentExpression: Boolean,
) : Statement() {
    override fun dumpToString(): String = buildString {
        if(hasThisExpression) {
            append("this.")
        }
        if(hasParentExpression) {
            append("parent.")
        }
        append("${BackticksPolitics.forIdentifier(name)}(")
        if(arguments != null) {
            val args = arguments.map { it.dumpToString() }.toMutableList()
            append(args.joinToString(separator = ", "))
        }
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

data class VariableDeclaration(
    val variable: VariableWithInitialValue
) : Statement() {
    override fun dumpToString(): String = buildString {
        append(variable.dumpToString())
    }
}
