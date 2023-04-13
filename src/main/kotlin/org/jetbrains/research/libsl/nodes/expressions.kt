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
    ADD("+"), COMP_ADD("+="), SUB("-"), COMP_SUB("-="), MUL("*"), COMP_MUL("*="),
    DIV("/"), COMP_DIV("/="), AND("&"), COMP_AND("&="), BIT_OR("|"), COMP_OR("|="),
    LOG_OR("||"), XOR("^"), COMP_XOR("^="), MOD("%"), COMP_MOD("%="), ASSIGN_OP("="),
    EQ("=="), NOT_EQ("!="), GT(">"), GT_EQ(">="), LT("<"), LT_EQ("<="), R_SHIFT(">>"),
    COMP_R_SHIFT(">>="), UNSIGNED_R_SHIFT(">>>"), COMP_UN_R_SHIFT(">>>="), L_SHIFT("<<"), COMP_L_SHIFT("<<=");
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

data class ActionExpression(
    val action: Action
) : Expression() {
    override fun dumpToString(): String = buildString {
        append("action ${BackticksPolitics.forIdentifier(action.name)}(")
        val args = action.arguments.map { it.dumpToString() }.toMutableList()
        append(args.joinToString(separator = ", "))
        append(")")
    }
}

data class ProcExpression(
    val proc: Proc
) : Expression() {
    override fun dumpToString(): String = buildString {
        append("${BackticksPolitics.forIdentifier(proc.name)}(")
        val args = proc.arguments.map { it.dumpToString() }.toMutableList()
        append(args.joinToString(separator = ", "))
        append(")")
    }
}
