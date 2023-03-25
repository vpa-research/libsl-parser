package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.AutomatonStateReference
import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class Expression: Node()
data class BinaryOpExpression(
    val left: Expression,
    val right: Expression,
    val op: ArithmeticBinaryOps
) : Expression() {
    override fun dumpToString(): String = "(${left.dumpToString()} ${op.string} ${right.dumpToString()})"
}

enum class ArithmeticBinaryOps(val string: String) {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), AND("&"),
    OR("|"), XOR("^"), MOD("%"), EQ("="), NOT_EQ("!="), GT(">"),
    GT_EQ(">="), LT("<"), LT_EQ("<=");
    companion object {
        fun fromString(str: String) = ArithmeticBinaryOps.values().first { op -> op.string == str }
    }
}

data class UnaryOpExpression(
    val value: Expression,
    val op: ArithmeticUnaryOp
) : Expression() {
    override fun dumpToString(): String = "${op.string}${value.dumpToString()}"
}

data class OldValue(
    val value: QualifiedAccess
) : Expression() {
    override fun dumpToString(): String = "${value.dumpToString()}'"
}

data class CallAutomatonConstructor(
    val automatonRef: AutomatonReference,
    val args: List<ArgumentWithValue>,
    val stateRef: AutomatonStateReference
) : Atomic() {
    override val value: Any? = null

    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("new ${BackticksPolitics.forPeriodSeparated(automatonRef.name)}")

        val formattedArgs = buildList {
            add("state = ${BackticksPolitics.forIdentifier(stateRef.name)}")
            for (arg in args) {
                add(arg.dumpToString())
            }
        }
        append(formattedArgs.joinToString(separator = ", ", prefix = "(", postfix = ")"))
    }
}

data class ArrayLiteral(
    override val value: List<Expression>
) : Atomic() {
    override fun dumpToString(): String {
        return buildString {
            append("[")
            append(
                value.joinToString(separator = ", ") { v -> v.dumpToString() }
            )
            append("]")
        }
    }
}

sealed class Atomic : Expression() {
    abstract val value: Any?

    override fun dumpToString(): String = value?.toString() ?: ""
}
